<img src="nourish-logo-and-title.png" width="300">

# Nourish: An LLM-integrated Mobile App to promote Home Cooking

Imogen Pemberton, The University of Sheffield (Academic Year 25-26)

COM3610 Dissertation Project

## Showcase Video

<a href="https://www.youtube.com/watch?v=KMuAeE98rdQ">
  <img src="nourish-thumbnail.png" width="600">
</a>

## Prerequisites

- Android Studio SDK (Nourish was created using version Narwahl 3)
- Access to an Android phone or emulator (emulator is provided in Android Studio) with minimum SDK API 33 (Tiramisu; Android 13.0). Optimal SDK for the project: API 36.0 (Baklava; Android 16.0).

## Setup and Installation

### Mobile Application

1. _(skip this step if you have been provided with the Nourish zip file)_ Clone the git repository using
   `git clone https://github.com/imoqen/Nourish`
2. Open the android-app directory in Android Studio with minimum SDK API 33 (Tiramisu; Android 13).
3. Set up and boot either the emulator or USB debugging on a real connected Android phone.
4. Press the play icon in the top right of Android studio. The app will then launch on the phone. This may take some time for the first launch, due to library installations.

### Google Cloud Service

The Google Cloud Service can only be established when authenticated with the Google Cloud project through the Google Cloud Console.
This requires:

- Authentication with a Google Cloud Project in the Google Cloud console

- Replacing the secret keys throughout the code with the Google Cloud project keys:
  `GOOGLE_CLOUD_URL`
  `WEB_CLIENT_ID`
  `GOOGLE_CLOUD_PROJECT_NAME`
  `GOOGLE_CLOUD_PROJECT_REGION`

- Initialising the Google Cloud connection through creation of a `google-services.json` file in Put path here according to the Google Cloud documentation (add googles link here).

Once Google Cloud is connected, the service can be established using the following commands while in the `google-cloud-backend` directory:

First, ensure all libraries are installed:

`npm install`

Then bring up the GCS, replacing GOOGLE_CLOUD_SERVICE_NAME and GOOGLE_CLOUD_REGION with the GCS' name and region, respectively:

`gcloud run deploy GOOGLE_CLOUD_SERVICE_NAME --source . --region GOOGLE_CLOUD_REGION --allow-unauthenticated`

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
