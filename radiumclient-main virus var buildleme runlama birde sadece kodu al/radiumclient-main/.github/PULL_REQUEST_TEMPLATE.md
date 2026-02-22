## Pull Request Checklist

Before submitting this PR, please ensure:

- [ ] No auth-related code has been added
- [ ] All CI checks pass
- [ ] Code has been tested
- [ ] No security-related files are included

### Auth Code Check

This PR will be automatically scanned for auth code. If auth code is detected, the PR will be blocked.

**Forbidden patterns:**
- `license.txt`, `Scrambler`, `HWID`, `Packets`
- `com.radium.client.security` package
- Auth server connections
- License verification code

### Description

<!-- Describe your changes here -->

### Changes Made

<!-- List the changes made in this PR -->

### Testing

<!-- Describe how you tested these changes -->

---

**Note**: This repository does not allow authentication or license verification code. All PRs are automatically scanned.

