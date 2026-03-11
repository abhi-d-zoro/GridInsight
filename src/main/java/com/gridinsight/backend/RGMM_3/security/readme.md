# RGMM_3 Security Package

This package contains security configuration for asset management endpoints.

## Files
- `AssetSecurityConfig.java`: Restricts asset registration to users with the `ASSET_MANAGER` role.

## Testing
- In Postman, attempt to register an asset as a user without the `ASSET_MANAGER` role and verify access is denied (403 Forbidden).
- Register as an Asset Manager and verify access is granted.

