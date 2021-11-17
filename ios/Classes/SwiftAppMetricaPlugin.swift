import Flutter
import UIKit
import YandexMobileMetrica

public class SwiftAppMetricaPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "app_metrica_plugin", binaryMessenger: registrar.messenger())
    let instance = SwiftAppMetricaPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let method = call.method
    switch method {
    case "activate":
        let args = call.arguments as! [String: Any]
        let apiKey = args["apiKey"] as! String
        let configuration = YMMYandexMetricaConfiguration.init(apiKey: apiKey)

        YMMYandexMetrica.activate(with: configuration!)

        result(nil)
    case "reportEvent":
        let args = call.arguments as! [String: Any]
        let attributes = args["attributes"] as? [String : Any]
        let name = args["name"] as! String

        if attributes != nil {
            YMMYandexMetrica.reportEvent(name, parameters: attributes, onFailure: { err in
                print("Failed to send event: \(err)")
            })
        } else {
            YMMYandexMetrica.reportEvent(name, onFailure: { err in
                print("Failed to send event: \(err)")
            })
        }

        result(nil)
    case "showProductCardEvent":
        let args = call.arguments as! [String: Any]
        let productActualPrice = args["actualPrice"] as! Int
        let productOriginalPrice = args["productOriginalPrice"] as! Int
        let screenWhereFromOpen = args["screenWhereFromOpen"] as! String
        let productName = args["productName"] as! String
        let productID = args["productID"] as! String
        
        let screen = YMMECommerceScreen(name: screenWhereFromOpen)
        
        let actualPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productActualPrice)")))
        
        let originalPrice = YMMECommercePrice(fiat: .init(unit: "RUB", value: .init(string: "\(productOriginalPrice)")))
        
        let product = YMMECommerceProduct(
            sku: productID,
            name: productName,
            categoryComponents: nil,
            payload: nil,
            actualPrice: actualPrice,
            originalPrice: originalPrice,
            promoCodes: nil)
        
        YMMYandexMetrica.report(eCommerce: .showProductCardEvent(product: product, screen: screen), onFailure: nil)
        
        result(nil)
    case "showScreenEvent":
        let args = call.arguments as! [String: Any]
        let screenWhereFromOpen = args["screenWhereFromOpen"] as! String
        let screen = YMMECommerceScreen(name: screenWhereFromOpen)
        
        YMMYandexMetrica.report(eCommerce: .showScreenEvent(screen: screen), onFailure: nil)
        result(nil)
    default:
        result(nil)
    }
  }
}
