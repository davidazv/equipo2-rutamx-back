# 3D Asset Upload via Admin Console — Required Changes

GLB files are gitignored. They need to be uploaded/served through the app instead.

## Current State

- **Backend:** `BusModel` has no URL fields for 3D models or images. No file storage. Upload system is CSV-only.
- **Frontend:** GLB files hardcoded in `bus-carousel-3d.tsx` array, mapped by index. No upload UI for assets.

## Backend Changes

- [ ] Add `model_3d_url` and `image_url` columns to `bus_models` table
- [ ] Add matching fields to `BusModelEntity`, `BusModel`, `BusModelMapper`
- [ ] Create `AssetStorageService` (Firebase Storage recommended — already in project for auth)
- [ ] Add `POST /api/bus-models/{id}/assets` endpoint (multipart, accepts `type=model3d|image`)
- [ ] Support optional URL columns in CSV import

## Frontend Changes

- [ ] Extend `BusModel` type with `model3dUrl`, `imageUrl`
- [ ] Add `uploadBusModelAsset()` API function
- [ ] Add file inputs to `BusModelCreateModal` and `BusModelEditModal`
- [ ] Replace hardcoded `GLB_FILES` array in `bus-carousel-3d.tsx` with dynamic URLs from bus model data
- [ ] Add `*.glb` to `.gitignore`
- [ ] Remove GLB files from `public/` after migration
