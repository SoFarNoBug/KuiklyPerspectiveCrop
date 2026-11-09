/**
 * Minimal OH Native Drawing declarations for perspective crop (SetPolyToPoly).
 * Links libnative_drawing.so. Opaque OH_PixelmapNative matches ohos_image_min.h.
 */
#pragma once

#include <stdbool.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct OH_Drawing_Canvas OH_Drawing_Canvas;
typedef struct OH_Drawing_Bitmap OH_Drawing_Bitmap;
typedef struct OH_Drawing_Matrix OH_Drawing_Matrix;
typedef struct OH_Drawing_Rect OH_Drawing_Rect;
typedef struct OH_Drawing_PixelMap OH_Drawing_PixelMap;
typedef struct OH_Drawing_SamplingOptions OH_Drawing_SamplingOptions;
typedef struct OH_PixelmapNative OH_PixelmapNative;

typedef struct {
    float x;
    float y;
} OH_Drawing_Point2D;

typedef enum {
    COLOR_FORMAT_UNKNOWN_MIN = 0,
    COLOR_FORMAT_ALPHA_8_MIN = 1,
    COLOR_FORMAT_RGB_565_MIN = 2,
    COLOR_FORMAT_ARGB_4444_MIN = 3,
    COLOR_FORMAT_RGBA_8888_MIN = 4,
    COLOR_FORMAT_BGRA_8888_MIN = 5,
} OH_Drawing_ColorFormat_Min;

typedef enum {
    ALPHA_FORMAT_UNKNOWN_MIN = 0,
    ALPHA_FORMAT_OPAQUE_MIN = 1,
    ALPHA_FORMAT_PREMUL_MIN = 2,
    ALPHA_FORMAT_UNPREMUL_MIN = 3,
} OH_Drawing_AlphaFormat_Min;

typedef struct {
    OH_Drawing_ColorFormat_Min colorFormat;
    OH_Drawing_AlphaFormat_Min alphaFormat;
} OH_Drawing_BitmapFormat_Min;

OH_Drawing_Bitmap *OH_Drawing_BitmapCreate(void);
void OH_Drawing_BitmapDestroy(OH_Drawing_Bitmap *bitmap);
void OH_Drawing_BitmapBuild(
    OH_Drawing_Bitmap *bitmap,
    const uint32_t width,
    const uint32_t height,
    const OH_Drawing_BitmapFormat_Min *bitmapFormat
);
void *OH_Drawing_BitmapGetPixels(OH_Drawing_Bitmap *bitmap);

OH_Drawing_Canvas *OH_Drawing_CanvasCreate(void);
void OH_Drawing_CanvasDestroy(OH_Drawing_Canvas *canvas);
void OH_Drawing_CanvasBind(OH_Drawing_Canvas *canvas, OH_Drawing_Bitmap *bitmap);
void OH_Drawing_CanvasSetMatrix(OH_Drawing_Canvas *canvas, OH_Drawing_Matrix *matrix);
void OH_Drawing_CanvasDrawPixelMapRect(
    OH_Drawing_Canvas *canvas,
    OH_Drawing_PixelMap *pixelMap,
    const OH_Drawing_Rect *src,
    const OH_Drawing_Rect *dst,
    const OH_Drawing_SamplingOptions *samplingOptions
);

OH_Drawing_Matrix *OH_Drawing_MatrixCreate(void);
void OH_Drawing_MatrixDestroy(OH_Drawing_Matrix *matrix);
bool OH_Drawing_MatrixSetPolyToPoly(
    OH_Drawing_Matrix *matrix,
    const OH_Drawing_Point2D *src,
    const OH_Drawing_Point2D *dst,
    uint32_t count
);

OH_Drawing_Rect *OH_Drawing_RectCreate(float left, float top, float right, float bottom);
void OH_Drawing_RectDestroy(OH_Drawing_Rect *rect);

OH_Drawing_PixelMap *OH_Drawing_PixelMapGetFromOhPixelMapNative(OH_PixelmapNative *pixelmapNative);
void OH_Drawing_PixelMapDissolve(OH_Drawing_PixelMap *pixelMap);

#ifdef __cplusplus
}
#endif
