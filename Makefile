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
    ./scripts/rebuild_ios.sh

ios-open:
    open iosApp/iosApp.xcodeproj

clean:
    ./gradlew clean
    rm -rf iosApp/DerivedData shared/build/xcode-frameworks


pcm:
	python3 tools/pcm_update.py

check-pcm:
	python3 tools/pcm_update.py --check
