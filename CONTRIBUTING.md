# Contributing to Pixel Fish Tank

Thank you for your interest in contributing to Pixel Fish Tank! This document provides guidelines and instructions for contributing.

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Respect different viewpoints and experiences

## How to Contribute

### Reporting Bugs

1. Check if the bug has already been reported in the issue tracker
2. If not, create a new issue with:
   - Clear, descriptive title
   - Steps to reproduce the bug
   - Expected behavior
   - Actual behavior
   - Device/OS information
   - Screenshots (if applicable)

### Suggesting Features

1. Check if the feature has already been suggested
2. Create a new issue with:
   - Clear description of the feature
   - Use case and motivation
   - Potential implementation approach (if you have ideas)

### Pull Requests

1. **Fork the repository**

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow the code style guidelines
   - Write clear, meaningful commit messages
   - Test your changes thoroughly

4. **Commit your changes**
   ```bash
   git commit -m "Add: Description of your changes"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Provide a clear description of your changes
   - Reference any related issues
   - Include screenshots for UI changes

## Development Setup

1. Fork and clone the repository
2. Open in Android Studio
3. Add your `google-services.json` (see README.md)
4. Sync Gradle and build
5. Run the app on an emulator or device

## Code Style Guidelines

### Kotlin
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Prefer `val` over `var` when possible
- Use data classes for models
- Keep functions focused and single-purpose

### Compose
- Use descriptive composable function names
- Extract reusable components
- Use `remember` and `LaunchedEffect` appropriately
- Follow Material Design 3 guidelines

### Architecture
- Follow MVVM pattern
- Keep business logic in ViewModels
- Use repositories for data access
- Keep UI components stateless when possible

### File Organization
```
app/src/main/java/com/charles/virtualpet/fishtank/
├── data/          # Data layer (repositories, stores)
├── domain/        # Business logic (ViewModels, models, use cases)
└── ui/            # UI layer (screens, components)
```

## Commit Message Guidelines

Use clear, descriptive commit messages:

- **Add**: New features
- **Fix**: Bug fixes
- **Update**: Changes to existing features
- **Refactor**: Code improvements without changing functionality
- **Docs**: Documentation changes
- **Style**: Formatting, missing semicolons, etc.
- **Test**: Adding or updating tests

Example:
```
Add: Daily task completion rewards
Fix: Tank cleaning not updating fish happiness
Update: Improve mini-game scoring algorithm
```

## Testing

- Test your changes on multiple Android versions (API 24+)
- Test on different screen sizes
- Verify offline functionality
- Check for memory leaks and performance issues

## Review Process

1. All pull requests will be reviewed
2. Address any feedback or requested changes
3. Maintainers will merge approved PRs

## Questions?

If you have questions, feel free to:
- Open an issue for discussion
- Contact the maintainers

Thank you for contributing to Pixel Fish Tank! 🐠

