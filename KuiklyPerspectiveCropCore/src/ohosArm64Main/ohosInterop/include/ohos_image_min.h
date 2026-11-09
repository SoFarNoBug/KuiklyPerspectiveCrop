/**
 * Minimal OH Image NDK declarations for Kotlin/Native cinterop.
 * Decode → ARGB（出图编码走软件 PNG，不链 ImagePacker Native，避免 SIGSEGV）。
 */
#pragma once

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
    IMAGE_SUCCESS_MIN = 0,
} Image_ErrorCode;

typedef struct Image_Size {
    uint32_t width;
    uint32_t height;
} Image_Size;

typedef struct Image_String {
    char *data;
    size_t size;
} Image_String;

typedef struct Image_String Image_MimeType;

enum {
    PIXEL_FORMAT_RGBA_8888_MIN = 3,
    PIXEL_FORMAT_BGRA_8888_MIN = 4,
};

typedef struct OH_ImageSourceNative OH_ImageSourceNative;
typedef struct OH_ImageSource_Info OH_ImageSource_Info;
typedef struct OH_DecodingOptions OH_DecodingOptions;
typedef struct OH_PixelmapNative OH_PixelmapNative;
typedef struct OH_Pixelmap_ImageInfo OH_Pixelmap_ImageInfo;

Image_ErrorCode OH_ImageSourceInfo_Create(OH_ImageSource_Info **info);
Image_ErrorCode OH_ImageSourceInfo_GetWidth(OH_ImageSource_Info *info, uint32_t *width);
Image_ErrorCode OH_ImageSourceInfo_GetHeight(OH_ImageSource_Info *info, uint32_t *height);
Image_ErrorCode OH_ImageSourceInfo_Release(OH_ImageSource_Info *info);

Image_ErrorCode OH_DecodingOptions_Create(OH_DecodingOptions **options);
Image_ErrorCode OH_DecodingOptions_SetPixelFormat(OH_DecodingOptions *options, int32_t pixelFormat);
Image_ErrorCode OH_DecodingOptions_SetRotate(OH_DecodingOptions *options, float rotate);
Image_ErrorCode OH_DecodingOptions_SetDesiredSize(OH_DecodingOptions *options, Image_Size *desiredSize);
Image_ErrorCode OH_DecodingOptions_Release(OH_DecodingOptions *options);

Image_ErrorCode OH_ImageSourceNative_CreateFromUri(char *uri, size_t uriSize, OH_ImageSourceNative **res);
Image_ErrorCode OH_ImageSourceNative_GetImageInfo(OH_ImageSourceNative *source, int32_t index, OH_ImageSource_Info *info);
Image_ErrorCode OH_ImageSourceNative_CreatePixelmap(
    OH_ImageSourceNative *source,
    OH_DecodingOptions *options,
    OH_PixelmapNative **pixelmap
);
Image_ErrorCode OH_ImageSourceNative_Release(OH_ImageSourceNative *source);

Image_ErrorCode OH_PixelmapImageInfo_Create(OH_Pixelmap_ImageInfo **info);
Image_ErrorCode OH_PixelmapImageInfo_GetWidth(OH_Pixelmap_ImageInfo *info, uint32_t *width);
Image_ErrorCode OH_PixelmapImageInfo_GetHeight(OH_Pixelmap_ImageInfo *info, uint32_t *height);
Image_ErrorCode OH_PixelmapImageInfo_Release(OH_Pixelmap_ImageInfo *info);

Image_ErrorCode OH_PixelmapNative_GetImageInfo(OH_PixelmapNative *pixelmap, OH_Pixelmap_ImageInfo *imageInfo);
Image_ErrorCode OH_PixelmapNative_GetArgbPixels(OH_PixelmapNative *pixelmap, uint8_t *destination, size_t *bufferSize);
Image_ErrorCode OH_PixelmapNative_Release(OH_PixelmapNative *pixelmap);

/* zlib（PNG IDAT）；与系统 libz.so 链接 */
unsigned long compressBound(unsigned long sourceLen);
int compress2(
    unsigned char *dest,
    unsigned long *destLen,
    const unsigned char *source,
    unsigned long sourceLen,
    int level
);

#ifdef __cplusplus
}
#endif
