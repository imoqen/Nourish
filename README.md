<img src="nourish-logo-and-title.png" width="300">

# Nourish: An LLM-integrated Mobile App to promote Home Cooking

Imogen Pemberton, The University of Sheffield (Academic Year 25-26)

COM3610 Dissertation Project

## Showcase Video

<iframe width="560" height="315" src="https://www.youtube.com/embed/KMuAeE98rdQ?si=s3Jtcbi5Zos-XK7Q" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

## Prerequisites

- Android Studio SDK (Nourish was created using version Narwahl 3)
- Access to an Android phone or emulator (emulator is provided in Android Studio) with minimum SDK API 33 (Tiramisu; Android 13.0). Optimal SDK for the project: API 36.0 (Baklava; Android 16.0).

## Setup

### Mobile Application

1. _(skip this step if you have been provided with the Nourish zip file)_ Clone the git repository using
   `git clone https://github.com/imoqen/Nourish`
2. Open the project in Android Studio with minimum SDK API 33 (Tiramisu; Android 13).
3. Set up and boot either the emulator or USB debugging on a real connected Android phone.
4. Press the play icon in the top right of Android studio. The app will then launch on the phone. This may take some time for the first launch, due to library installations.

### Google Cloud Service

The Google Cloud Service can be established using the following command while in the `google-cloud-backend` directory:

`gcloud run deploy llm --source . --region GOOGLE_CLOUD_PROJECT_REGION --allow-unauthenticated`

This is only possible when authenticated with the GOOGLE_CLOUD_PROJECT_NAME project through the Google Cloud Console.

## Testing Execution

All tests must be executed from within the `android-app` directory.

Once successfully executed, JaCoCo coverage reports are generated within the `android-app/app/build/customJacocoReportDir` directory. These can be accessed in the `index.html` of each generated subdirectory.

### Unit tests

`./gradlew JacocoUnitCodeCoverage`

### Instrumented tests

Requires a device capable of executing the tests to be connected (physical Android or emulator).

` ./gradlew JacocoInstrumentedCodeCoverage`

## Acknowledgements

**Supervisor:** Tahsinur Khan

**Technical Advisor:** Shaun Donnelly

**Languages, Libraries and Services:** [Kotlin](https://kotlinlang.org/) with [Jetpack Compose](https://developer.android.com/compose), [LangChain](https://www.langchain.com/), [Google Cloud](https://cloud.google.com/), [Calvin Reorderable](https://github.com/Calvin-LL/Reorderable), [JaCoCo](https://www.eclemma.org/jacoco/)
