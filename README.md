# Assume

Response Mocking for Retrofit using annotations. Assume Provides safe and easy way to mock API responses for retrofit-okhttp3.

## Download
Add [Jitpack](https://jitpack.io/) repository in project build.gradle file

```

allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}

```

Add dependencies to your module's build.gradle file:

```

def assume_version = "0.0-beta02"

// kaptDebug to reduce unnecessary Annotation processing for release build generations
kaptDebug "com.github.aniketbhoite.Assume:processor:$assume_version"

// debugImplementation ensures APIs are mocked only in debug making it production safe
debugImplementation "com.github.aniketbhoite.Assume:annotations:$assume_version"

releaseImplementation "com.github.aniketbhoite.Assume:annotations-empty:$assume_version"

```

*annotations-empty* package adds empty placeholder classes. If you add annotation-empty then assume won't interfere with the actual response even if you keep the assume code in your codebase by mistake.

## Usage

Add @Assume annotation to API method with wanted response, responseCode is optional

```

@Assume(
	responseCode = 200,
	response ="{\"page\":1,\"results\":[],\"total_pages\":10,\"total_results\":100}"
)
@POST("v2/top-headlines")
suspend fun getHeadline(): NewsResponse

```

Add **AssumeInterceptor** inside the Okhttp3 client

```

val client = OkHttpClient.Builder()
        .addInterceptor(AssumeInterceptor(BASE_URL) //same base url added in retrofit
        .build()

```

You should be good to go.👍🏽

### Benefits of Assume

1. Saves hassles of modifying interceptors to mock response every time
2. Can mock multiple APIs

### Gotchas to keep in mind:

- Assume ignores Query params
- Assume doesn’t support API's with *@Url* (workaround: add API endpoint in retrofit method annotation)
- Assume is not perfect and we are working on it to make it better and easy to use. It should work with most of the endpoints and it is production safe

For any bugs or doubts raise [issue](https://github.com/aniketbhoite/Assume/issues) & for any changes or improvements start a [pull request](https://github.com/aniketbhoite/Assume/pulls)
