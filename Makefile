.PHONY: android-build android-install android-test android-lint ios-bootstrap ios-open clean

android-build:
	./gradlew :app:assembleDebug

android-install:
	./gradlew :app:installDebug

android-test:
	./gradlew :app:testDebugUnitTest

android-lint:
	./gradlew :app:lintDebug

ios-bootstrap:
	chmod +x scripts/ios_bootstrap.sh
	./scripts/ios_bootstrap.sh

ios-open:
	open iosApp/iosApp.xcworkspace

clean:
	./gradlew clean
	rm -rf iosApp/Pods iosApp/iosApp.xcworkspace iosApp/DerivedData

