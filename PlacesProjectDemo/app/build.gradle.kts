plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.placesprojectdemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.placesprojectdemo"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment:2.5.3")
    implementation("androidx.navigation:navigation-ui:2.5.3")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
//    implementation("com.google.firebase:firebase-database:20.3.0")
//    implementation("com.google.firebase:firebase-auth:22.3.0")
//    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //to check the permission
    implementation ("com.karumi:dexter:6.2.3")
    //to access map
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    //for searchbar
    implementation ("com.github.mancj:MaterialSearchBar:0.8.2")
    implementation ("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("com.google.android.libraries.places:places:3.2.0")
    //1.1.0
    implementation ("com.skyfishjy.ripplebackground:library:1.0.1")
    implementation ("com.google.android.gms:play-services-location:17.0.0")

    implementation ("com.squareup.picasso:picasso:2.71828")


}
