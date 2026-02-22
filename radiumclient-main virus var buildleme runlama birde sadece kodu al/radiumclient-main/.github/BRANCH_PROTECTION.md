# Branch Protection Setup Guide

To prevent auth code from being added, set up branch protection rules on GitHub:

## Steps to Enable Branch Protection

1. Go to your GitHub repository
2. Click **Settings** → **Branches**
3. Under **Branch protection rules**, click **Add rule**
4. Set **Branch name pattern** to `*` (protects all branches)
5. Enable these settings:

### Required Settings:
- ✅ **Require a pull request before merging**
  - Require approvals: 1
  - Dismiss stale pull request approvals when new commits are pushed
  - Require review from Code Owners (if you have CODEOWNERS file)

- ✅ **Require status checks to pass before merging**
  - Select: `check-auth` (the CI workflow we created)
  - Require branches to be up to date before merging

- ✅ **Require conversation resolution before merging**

- ✅ **Do not allow bypassing the above settings**
  - Even administrators cannot bypass

### Optional but Recommended:
- ✅ **Require linear history**
- ✅ **Include administrators**
- ✅ **Restrict who can push to matching branches** (if you have a team)

## Additional Protection

### Code Owners File
Create `.github/CODEOWNERS`:
```
* @your-username
src/main/java/com/radium/client/ @your-username
```

This ensures you review all changes to critical paths.

### Required Reviews
Set up required reviewers for sensitive areas:
- All Java files in `src/main/java/com/radium/client/`
- Any new files in security-related directories

## What This Prevents

1. **Direct pushes to protected branches** - All changes must go through PRs
2. **Merging without CI checks** - Auth detection CI must pass
3. **Bypassing protection** - Even admins can't bypass (if enabled)
4. **Force pushes** - Can be disabled in branch protection

## Testing

After setting up:
1. Try to push directly to master - should be blocked
2. Create a PR with auth code - CI should fail
3. Try to merge without approval - should be blocked

