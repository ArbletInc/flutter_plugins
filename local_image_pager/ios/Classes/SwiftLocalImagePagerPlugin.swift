import Flutter
import UIKit
import Photos
import PhotosUI

public class SwiftLocalImagePagerPlugin: NSObject, FlutterPlugin {
    private let error_code_param = "-1"
    private let error_code_catch_exception = "-2"

    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "local_image_pager", binaryMessenger: registrar.messenger())
        let instance = SwiftLocalImagePagerPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "getTotalNumber" {
            let options = PHFetchOptions()
            options.includeHiddenAssets = true
            let allPhotos = PHAsset.fetchAssets(with: .image, options: options)
            result(allPhotos.count)
        } else if call.method == "getLatestImages" {
            guard let args = call.arguments else {
                result(FlutterError(code: error_code_param, message: "getLatestImages:param error. args are null.", details: nil))
                return
            }
            
            if let myArgs = args as? [String: Any],
               let start = myArgs["start"] as? Int,
               let end = myArgs["end"] as? Int {
                fetchPhotos(start: start, end: end, result: result)
            } else {
                result(FlutterError(code: error_code_param, message: "getLatestImages:param error. 'start', 'end' params are required, must be int.", details: nil))
            }
        } else {
            result(FlutterMethodNotImplemented)
        }
    }

    fileprivate func fetchPhotos(start: Int, end: Int, result: @escaping FlutterResult) {
        if (start > end) {
                result(FlutterError(code: error_code_param, message: "getLatestImages:param error. start > end.", details: nil))
            return
        }

        let indexSet = IndexSet(Array(start...end))
        let total = end - start + 1
        
        let options = PHFetchOptions()
        options.includeHiddenAssets = true
        options.sortDescriptors = [NSSortDescriptor(key:"creationDate", ascending: false)]
        let allPhotos = PHAsset.fetchAssets(with: .image, options: options)
        var photosJson = [String?]()
        DispatchQueue.global(qos: .background).async { [weak self] in
            allPhotos.enumerateObjects(at: indexSet, options: NSEnumerationOptions.concurrent, using: { (asset, count, stop) in
                if ( asset.mediaType == PHAssetMediaType.image) {
                    let options: PHContentEditingInputRequestOptions = PHContentEditingInputRequestOptions()
                    options.canHandleAdjustmentData = {(adjustmeta: PHAdjustmentData) -> Bool in
                        return true
                    }
                    
                    asset.requestContentEditingInput(with: options, completionHandler: { (contentEditingInput, info) in
                        photosJson.append(contentEditingInput?.fullSizeImageURL?.path)
                        if photosJson.count == total {
                            result( photosJson )
                        }
                    })
                }
                
            })
        }
        
    }

}
