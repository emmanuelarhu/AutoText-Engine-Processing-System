# AutoText-Engine-Processing-System

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-21.0.6-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-green)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

## Overview

AutoText is a comprehensive JavaFX-based desktop application for text processing, analysis, and transformation. It provides a unified environment for working with text files individually or in batches, applying regular expressions, and analyzing text content through various metrics and transformations.

## Key Features

### Text Processing
![Text Processing Tab](https://github.com/user-attachments/assets/72080002-5fc8-47f6-815b-14a8a7a99475)


- **Text Analysis**: Count characters, words, lines, and sentences
- **Case Transformation**: Convert text to uppercase, lowercase, or title case
- **Format Beautifying**: Automatically format JSON and XML data
- **Content Extraction**: Identify and extract emails, URLs, and other patterns
- **Whitespace Handling**: Remove duplicate lines and normalize whitespace
- **Text Summarization**: Generate concise summaries from longer texts

### Regex Operations
![Regex Operations Tab](https://github.com/user-attachments/assets/7e6ba594-35be-4831-ae97-ba99f62f1a4f)


- **Pattern Testing**: Live testing of regular expressions with result highlighting
- **Pattern Library**: Save, organize, and reuse regex patterns
- **Flexible Configuration**: Set case sensitivity, multiline, and dotAll options
- **Find & Replace**: Perform regex-based search and replacement
- **Pattern Export/Import**: Share pattern libraries between installations
- **Context-Sensitive Help**: Get assistance with pattern creation and testing

### Batch Processing
![Batch Processing Tab](https://github.com/user-attachments/assets/67ef3fb9-6422-41c1-8bb8-7d03dc9929b4)


- **Multiple File Processing**: Apply operations to sets of files simultaneously
- **Filtering Options**: Select files by extension, name pattern, or size
- **Directory Traversal**: Process entire directory structures
- **Progress Tracking**: Monitor batch operations with detailed progress information
- **Custom Output Naming**: Configure output formats with variable substitution
- **Operation Queue**: Chain multiple operations for complex transformations

## Technical Architecture

AutoText Engine follows the Model-View-Controller (MVC) architecture pattern:

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── autotextengine/
│   │           ├── controller/    # Application logic
│   │           ├── model/         # Data structures
│   │           ├── service/       # Business operations
│   │           ├── util/          # Helper utilities
│   │           └── view/          # UI components
│   └── resources/
│       ├── css/                   # Styling
│       ├── fxml/                  # Layout definitions
│       └── images/                # Graphics assets
└── test/                          # Unit and integration tests
```

### Key Components

- **Models**:
  - `TextData`: Core text container with analysis methods
  - `RegexPattern`: Encapsulates regex patterns with configuration
  - `ProcessedResult`: Holds operation results in various formats

- **Services**:
  - `TextProcessingService`: Handles text manipulation operations
  - `RegexService`: Manages regular expression operations
  - `FileService`: Handles file I/O operations

- **Controllers**:
  - `FileController`: Manages file operations and processing
  - `RegexController`: Handles regex pattern testing and management
  - `TextProcessingController`: Controls text transformation operations

- **Views**:
  - `MainView`: Primary application interface with tab navigation
  - `RegexView`: Pattern testing and management interface
  - `FileProcessingView`: Text processing interface
  - `BatchProcessingView`: Batch operations interface

## Installation

### Prerequisites
- Java Development Kit (JDK) 21.0.6 or higher
- JavaFX SDK 21.0.6
- Log4j 2.24.3
- JUnit 5 for running tests

### Setup Options

#### Option 1: Using Pre-built Binary
1. Download the latest release from the releases page
2. Extract the archive to your preferred location
3. Run the application using:
   ```
   java -jar AutoTextEngine.jar
   ```

#### Option 2: Building from Source
1. Clone the repository:
   ```
   git clone https://github.com/emmanuelarhu/AutoText-Engine-Processing-System.git
   ```
2. Navigate to the project directory:
   ```
   cd AutoText-Engine-Processing-System
   ```
3. Build using your IDE or command line tools
4. Run the application from your IDE or using:
   ```
   java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/AutoTextEngine.jar
   ```

## Usage Guide

### Text Processing Workflow

1. **Load Text**:
   - Type directly into the input area
   - Use "Load File" to import from a text file

2. **Select Operation**:
   - Choose from the operation dropdown menu
   - Configure operation-specific options if available

3. **Process Text**:
   - Click "Process" to execute the selected operation
   - Review results in the output area

4. **Save or Export**:
   - Save processed text to a file
   - Copy to clipboard for use in other applications

### Regex Pattern Testing

1. **Create or Select Pattern**:
   - Choose a pattern type from the dropdown
   - Enter a custom pattern
   - Select from saved patterns

2. **Configure Options**:
   - Set case sensitivity
   - Enable multiline mode
   - Enable dot-all mode

3. **Test Pattern**:
   - Enter sample text
   - Click "Test Pattern" to validate
   - Click "Find Matches" to see all matches

4. **Replace Text**:
   - Enter replacement pattern
   - Click "Replace" to transform text

5. **Manage Patterns**:
   - Save useful patterns to the library
   - Organize with names and descriptions
   - Import/export pattern collections

### Batch Processing

1. **Select Files**:
   - Browse for input directory
   - Set file filters (extensions, name patterns, size)
   - Add individual files or entire directories

2. **Configure Processing**:
   - Choose operation to perform
   - Set output directory
   - Configure naming format

3. **Execute Processing**:
   - Click "Start Processing" to begin
   - Monitor progress in real-time
   - View results per file

4. **Save Configuration**:
   - Save frequently used configurations
   - Load saved configurations for repeat tasks

## Tips and Best Practices

- **Performance Optimization**:
  - For very large files, use batch processing with appropriate file size filters
  - Close unused tabs to reduce memory usage
  - Use the "Remove Extra Whitespace" operation to normalize text before other operations

- **Regex Patterns**:
  - Start with predefined patterns and modify as needed
  - Test patterns with small samples before applying to large datasets
  - Use the library to build a collection of frequently used patterns

- **Workflow Integration**:
  - Use batch processing for automated workflows
  - Export results in formats compatible with other tools
  - Create configurations for common processing tasks

## Extending the Application

AutoText Engine is designed to be extensible. Developers can:

1. **Add New Operations**:
   - Create new operation implementations in the `service` package
   - Register operations in the appropriate controller
   - Add UI elements in the FXML files

2. **Enhance Pattern Library**:
   - Add specialized patterns for specific domains
   - Implement additional pattern categories
   - Create pattern import/export formats

3. **Customize the Interface**:
   - Modify CSS styles in the `resources/css` directory
   - Update FXML layouts to add or rearrange components
   - Create new views for specialized functionality

# Contributing

Contributions to AutoText Engine are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Implement your changes
4. Add tests for new functionality
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Code Style
Please follow the existing code style and include appropriate tests for your features.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact and Support

- **Issue Tracker**: [GitHub Issues](https://github.com/emmanuelarhu/AutoText-Engine-Processing-System/issues)
- **Author**: Emmanuel Arhu
- **Email**: emmanuelarhu706@gmail.com
