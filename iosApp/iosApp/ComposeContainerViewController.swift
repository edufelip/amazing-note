import UIKit

final class ComposeContainerViewController: UIViewController {
    private var makeComposeController: (() -> UIViewController)?
    private var composeController: UIViewController?

    init(makeController: @escaping () -> UIViewController) {
        self.makeComposeController = makeController
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    func updateControllerFactory(_ factory: @escaping () -> UIViewController) {
        makeComposeController = factory
        installComposeIfPossible()
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        installComposeIfPossible()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        installComposeIfPossible()
    }

    private func installComposeIfPossible() {
        guard composeController == nil else { return }
        guard let factory = makeComposeController else { return }
        let controller = factory()
        addChild(controller)
        controller.view.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(controller.view)
        NSLayoutConstraint.activate([
            controller.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            controller.view.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            controller.view.topAnchor.constraint(equalTo: view.topAnchor),
            controller.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        controller.didMove(toParent: self)
        composeController = controller
    }
}
