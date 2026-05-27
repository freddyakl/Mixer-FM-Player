// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
}

tasks.register("downloadAppIcon") {
    doLast {
        val urls = listOf(
            "https://radio61.com/img/og.jpg",
            "https://radio61.com/og.jpg",
            "https://radio61.com/img/cover.png"
        )
        val targetDir = file("${project.rootDir}/app/src/main/res/drawable")
        if (!targetDir.exists()) targetDir.mkdirs()
        
        var downloaded = false
        for (urlString in urls) {
            try {
                println("TRYING URL: $urlString")
                val url = java.net.URL(urlString)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val outFile = file("$targetDir/og.jpg")
                    url.openStream().use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("DOWNLOAD_SUCCESS: Saved to ${outFile.absolutePath}")
                    downloaded = true
                    break
                } else {
                    println("URL $urlString returned status code: $responseCode")
                }
            } catch (e: Exception) {
                println("Failed for URL $urlString: ${e.message}")
            }
        }
        if (!downloaded) {
            println("DOWNLOAD_FAILED: None of the URL attempts succeeded.")
        }
    }
}
