import XCTest

final class AmazingNoteUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    func testLoginScreen() {
        let app = launch(screen: "login")
        XCTAssertTrue(app.otherElements[UITestIdentifiers.loginRoot].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textFields[UITestIdentifiers.loginEmail].exists)
        XCTAssertTrue(app.secureTextFields[UITestIdentifiers.loginPassword].exists)
        XCTAssertTrue(app.buttons[UITestIdentifiers.loginSubmit].exists)
    }

    func testHomeScreen() {
        let app = launch(screen: "notes")
        XCTAssertTrue(app.otherElements[UITestIdentifiers.homeRoot].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements[UITestIdentifiers.homeNotesList].exists)
        XCTAssertTrue(app.buttons[UITestIdentifiers.homeAddNote].exists)
        assertTabBarVisible(app)
    }

    func testFoldersScreen() {
        let app = launch(screen: "folders")
        XCTAssertTrue(app.otherElements[UITestIdentifiers.foldersRoot].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements[UITestIdentifiers.foldersGrid].exists)
        XCTAssertTrue(app.buttons[UITestIdentifiers.foldersAdd].exists)
        assertTabBarVisible(app)
    }

    func testNoteDetailScreen() {
        let app = launch(screen: "noteDetail")
        XCTAssertTrue(app.otherElements[UITestIdentifiers.noteDetailRoot].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textFields[UITestIdentifiers.noteTitle].exists)
        XCTAssertTrue(app.otherElements[UITestIdentifiers.noteEditor].exists)
        XCTAssertTrue(app.buttons[UITestIdentifiers.noteSave].exists)
    }

    func testSettingsScreen() {
        let app = launch(screen: "settings")
        XCTAssertTrue(app.otherElements[UITestIdentifiers.settingsRoot].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements[UITestIdentifiers.settingsThemeToggle].exists)
        XCTAssertTrue(app.otherElements[UITestIdentifiers.settingsLogin].exists)
        XCTAssertTrue(app.otherElements[UITestIdentifiers.settingsTrash].exists)
        XCTAssertTrue(app.otherElements[UITestIdentifiers.settingsPrivacy].exists)
        assertTabBarVisible(app)
    }

    private func assertTabBarVisible(_ app: XCUIApplication) {
        XCTAssertTrue(app.tabBars.firstMatch.waitForExistence(timeout: 2))
    }

    @discardableResult
    private func launch(screen: String) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["UITEST_MODE"] = "1"
        app.launchEnvironment["UITEST_SCREEN"] = screen
        app.launchEnvironment["UITEST_DARK"] = "0"
        app.launch()
        return app
    }
}
