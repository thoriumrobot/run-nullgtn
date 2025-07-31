# test-ribs
This is following steps on [uber/RIBs Android Tutorial](https://github.com/uber/RIBs/wiki/Android-Tutorial-1) to test Uber's cross-platform mobile architecture framework - RIBs

## Getting Started

### Prerequisites

* Java SE Development Kit (>=8)
* Android Emulator (suggest [Genymotion](https://www.genymotion.com) or [Bluestacks](https://www.bluestacks.com))
* [Android Studio](https://developer.android.com/studio/) (optional)
* [RIBs Code Generation Plugin for Android Studio and IntelliJ](https://github.com/uber/RIBs/wiki/Android-Tooling#ribs-code-generation-plugin-for-android-studio-and-intellij) (optional)

### Installing

Use **Android Studio** or execute the following command
```
./gradlew :app:installDebug
```
<table>
<tr>
<td><img src="https://user-images.githubusercontent.com/43363323/48781561-39875000-ed17-11e8-968c-e0799ee4b076.png" width="200"></td>
<td><img src="https://user-images.githubusercontent.com/43363323/48786276-7bb58f00-ed21-11e8-842d-9a82f4583fca.png" width="200"></td>
<td><img src="https://user-images.githubusercontent.com/43363323/48786387-bcada380-ed21-11e8-91cb-d86d172d94a9.png" width="200"></td>
<td><img src="https://user-images.githubusercontent.com/43363323/48786357-ad2e5a80-ed21-11e8-8e82-167dc08d7331.png" width="200"></td>
</tr>
</table>

## Test

Execute the following command
```
./gradlew :app:testDebugUnitTest
```
