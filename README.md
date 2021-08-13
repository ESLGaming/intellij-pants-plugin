# Pants Support (ESL version)

This is a fork of the [official IntelliJ Pants plugin](https://github.com/pantsbuild/intellij-pants-plugin/). It is maintained by [ESL](https://about.eslgaming.com) and includes several performance and usability improvements for large Scala mono repositories. 

## Installation

Install the plugin from the [Jetbrains Marketplace](https://plugins.jetbrains.com/plugin/17343-pants-support-esl-version-).

## Usage

Please check the [official guide](https://github.com/pantsbuild/intellij-pants-plugin/#user-documentation) how to use the plugin.

## Additional features / improvements

- Target metadata for each module is stored in a [persistent state component](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html). This improves importing speed from several minutes to seconds!
- When selecting a "Run" configuration, the plugin will automatically adjust the default classpath to the ":main" target if available. This allows to split code-base and run resources (like configurations).

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Changes will be applied internally and afterwards mirrored to Github. A new public release will be created afterwards.

## License
[MIT](https://choosealicense.com/licenses/mit/)