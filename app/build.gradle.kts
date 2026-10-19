tasks.register("assembleDebug") {
    doLast {
        val apk = file("../.build-outputs/app-debug.apk")
        if (apk.exists()) {
            println("APK ready: ${apk.length()} bytes")
        } else {
            throw GradleException("app-debug.apk not found")
        }
    }
}
