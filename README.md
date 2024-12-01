# SmartCSV: A Parallel CSV Processing Library for Spring Boot
[![](https://jitpack.io/v/saadzarook/smartcsv.svg)](https://jitpack.io/#saadzarook/smartcsv)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/saadzarook/smartcsv.svg)](https://github.com/saadzarook/smartcsv/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/saadzarook/smartcsv.svg)](https://github.com/saadzarook/smartcsv/network)
[![GitHub issues](https://img.shields.io/github/issues/saadzarook/smartcsv.svg)](https://github.com/saadzarook/smartcsv/issues)
![Java 17+](https://img.shields.io/badge/java-17+-blue.svg)
## Introduction

**SmartCSV** is a Spring Boot library designed to simplify the handling of CSV file uploads and processing. It allows developers to:

- **Upload CSV Files Easily**: Streamline CSV file uploads via REST endpoints.
- **Validate and Process Data in Parallel**: Improve performance by concurrently validating and processing records.
- **Define Custom Validations**: Specify custom validation rules for CSV headers and data fields.
- **Flexible Error Handling**: Choose how to handle validation errors—skip records, stop processing, or collect and return all errors.
- **Annotation-Based Configuration**: Integrate seamlessly into existing projects with minimal effort.

## Features

- **Annotation-Based Setup**: Use `@SmartCsvProcessor` and `@SmartCsvField` annotations to configure processing.
- **Custom Validations**: Implement your own validation logic for headers and records.
- **Parallel Processing**: Utilize multi-threading to process large datasets efficiently.
- **Flexible Error Handling**: Decide whether to skip invalid records, halt processing on errors, or collect errors for reporting.
- **Detailed Logging**: Monitor processing steps and errors through comprehensive SLF4J logging.

## Installation

Add the following dependency to your project's `pom.xml` or `build.gradle` file:
This project uses JitPack to host the library. To add the dependency to your project, follow the instructions below: 
**For Maven (`pom.xml`):**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
``` 
```xml
<dependency>
<groupId>com.github.saadzarook</groupId>
<artifactId>smartcsv</artifactId>
<version>v1.0.0</version>
</dependency>

``` 

For Gradle (`build.gradle`):

```
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.saadzarook:smartcsv:v1.0.0'
}
```

## Usage

1. Annotate your processing class with `@SmartCsvProcessor`
```java
@SmartCsvProcessor(
    model = User.class,
    validationStrategy = "collect", // Options: skip, stop, collect
    headerValidator = CustomHeaderValidator.class // Optional
)
public void processUser(User user) {
    // Your processing logic here
    userService.save(user);
}
```
2. Define Your Model Class
```java
public class User {
    @SmartCsvField(header = "Name", required = true)
    private String name;

    @SmartCsvField(header = "Email", required = true, validation = "[^@ ]+@[^@ ]+\\.[^@ ]+")
    private String email;

    // Getters and setters
}
```
3. Implement a Custom Header Validator (Optional)
```java
public class CustomHeaderValidator implements HeaderValidator {
    @Override
    public boolean validateHeaders(String[] headers) {
        return Arrays.asList(headers).contains("Name") && Arrays.asList(headers).contains("Email");
    }
}
```
4. Handle CSV File Uploads via REST Endpoint
```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private CsvProcessingService csvProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            // Get the method annotated with @SmartCsvProcessor
            Method processUserMethod = this.getClass().getMethod("processUser", User.class);

            // Process the CSV file
            List<RecordError> errors = csvProcessingService.processCsv(file, processUserMethod, this);

            if (!errors.isEmpty()) {
                // Return errors to the client
                return ResponseEntity.badRequest().body(errors);
            }

            return ResponseEntity.ok("CSV file processed successfully");
        } catch (Exception e) {
            // Handle exceptions
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @SmartCsvProcessor(
        model = User.class,
        validationStrategy = "collect",
        headerValidator = CustomHeaderValidator.class
    )
    public void processUser(User user) {
        // Processing logic
        userService.save(user);
    }
}
```

## Error Handling Strategies
Specify how the library should handle validation errors using the validationStrategy parameter:

- skip: Skip invalid records and continue processing.
- stop: Stop processing when an error is encountered.
- collect: Collect all errors and return them after processing.

## Contribution
Contributions are welcome! Feel free to open issues or submit pull requests to help improve this library.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact
For questions or support, please contact Saad Zarook at saadzarook@gmail.com
