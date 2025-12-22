import XCTest
import SnapshotTesting
import UIKit
@testable import iosApp

final class GoldenSnapshotTests: XCTestCase {
    private let config = ViewImageConfig.iPhone13

    override func setUp() {
        super.setUp()
        isRecording = false
    }

    func testLoginLight() {
        assertSnapshot(
            of: makeController(screen: .login, style: .light),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .light))
        )
    }

    func testLoginDark() {
        assertSnapshot(
            of: makeController(screen: .login, style: .dark),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .dark))
        )
    }

    func testHomeLight() {
        assertSnapshot(
            of: makeController(screen: .notes, style: .light),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .light))
        )
    }

    func testHomeDark() {
        assertSnapshot(
            of: makeController(screen: .notes, style: .dark),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .dark))
        )
    }

    func testFoldersLight() {
        assertSnapshot(
            of: makeController(screen: .folders, style: .light),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .light))
        )
    }

    func testFoldersDark() {
        assertSnapshot(
            of: makeController(screen: .folders, style: .dark),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .dark))
        )
    }

    func testNoteDetailLight() {
        assertSnapshot(
            of: makeController(screen: .noteDetail, style: .light),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .light))
        )
    }

    func testNoteDetailDark() {
        assertSnapshot(
            of: makeController(screen: .noteDetail, style: .dark),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .dark))
        )
    }

    func testSettingsLight() {
        assertSnapshot(
            of: makeController(screen: .settings, style: .light),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .light))
        )
    }

    func testSettingsDark() {
        assertSnapshot(
            of: makeController(screen: .settings, style: .dark),
            as: .image(on: config, traits: UITraitCollection(userInterfaceStyle: .dark))
        )
    }

    private func makeController(screen: SnapshotScreen, style: UIUserInterfaceStyle) -> UIViewController {
        let controller = makeSnapshotController(screen: screen, style: style)
        let size = config.size ?? CGSize(width: 390, height: 844)
        controller.view.frame = CGRect(origin: .zero, size: size)
        controller.view.layoutIfNeeded()
        return controller
    }
}
