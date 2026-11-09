#include "napi/native_api.h"
#include "libshared_api.h"
#include <cstdint>
#include <cstring>
#include <hilog/log.h>
#include <string>
#include <vector>

// Kotlin/Native @CName 生成签名为 void*（见 libshared_api.h）
extern "C" void perspective_crop_set_resfile_dir(void *dir);
extern "C" void perspective_crop_set_ohos_dirs(void *resfileDir, void *cacheDir);
extern "C" int perspective_crop_magnifier_blit_snapshot(
    void *generation, void *width, void *height, void *rgba, void *rgbaLen, void *borderWidth,
    void *borderColor, void *showCross, void *crossColor, void *crossLineWidth,
    void *crossLineLength, void *showCropLineMirror, void *cropLineColor, void *cropLineWidth,
    void *touchViewX, void *touchViewY, void *viewPointsXy, void *viewPointsLen);

static bool NapiOk(napi_status status)
{
    return status == napi_ok;
}

static void SetNamedInt(napi_env env, napi_value obj, const char *name, int32_t v)
{
    napi_value nv = nullptr;
    if (!NapiOk(napi_create_int32(env, v, &nv))) {
        return;
    }
    napi_set_named_property(env, obj, name, nv);
}

static void SetNamedDouble(napi_env env, napi_value obj, const char *name, double v)
{
    napi_value nv = nullptr;
    if (!NapiOk(napi_create_double(env, v, &nv))) {
        return;
    }
    napi_set_named_property(env, obj, name, nv);
}

static void SetNamedBool(napi_env env, napi_value obj, const char *name, bool v)
{
    napi_value nv = nullptr;
    if (!NapiOk(napi_get_boolean(env, v, &nv))) {
        return;
    }
    napi_set_named_property(env, obj, name, nv);
}

static bool IsNapiString(napi_env env, napi_value value)
{
    napi_valuetype t = napi_undefined;
    if (!NapiOk(napi_typeof(env, value, &t))) {
        return false;
    }
    return t == napi_string;
}

/** 成功写入 UTF-8（含尾 0）返回 true；类型/状态失败返回 false 且 out 置为空串缓冲。 */
static bool ReadUtf8Arg(napi_env env, napi_value arg, std::vector<char> &out)
{
    out.assign(1, '\0');
    if (arg == nullptr || !IsNapiString(env, arg)) {
        return false;
    }
    size_t length = 0;
    if (!NapiOk(napi_get_value_string_utf8(env, arg, nullptr, 0, &length))) {
        return false;
    }
    out.assign(length + 1, '\0');
    size_t written = 0;
    if (!NapiOk(napi_get_value_string_utf8(env, arg, out.data(), out.size(), &written))) {
        out.assign(1, '\0');
        return false;
    }
    return true;
}

static napi_value InitKuikly(napi_env env, napi_callback_info info)
{
    (void)info;
    auto api = libshared_symbols();
    int handler = api->kotlin.root.initKuikly();
    napi_value result = nullptr;
    if (!NapiOk(napi_create_int32(env, handler, &result))) {
        return nullptr;
    }
    return result;
}

static napi_value SetPerspectiveCropResfileDir(napi_env env, napi_callback_info info)
{
    size_t argc = 1;
    napi_value args[1] = {nullptr};
    if (!NapiOk(napi_get_cb_info(env, info, &argc, args, nullptr, nullptr)) || argc < 1) {
        return nullptr;
    }
    std::vector<char> buffer;
    if (!ReadUtf8Arg(env, args[0], buffer)) {
        return nullptr;
    }
    perspective_crop_set_resfile_dir(static_cast<void *>(buffer.data()));
    return nullptr;
}

static napi_value SetPerspectiveCropOhosDirs(napi_env env, napi_callback_info info)
{
    size_t argc = 2;
    napi_value args[2] = {nullptr, nullptr};
    if (!NapiOk(napi_get_cb_info(env, info, &argc, args, nullptr, nullptr)) || argc < 1) {
        return nullptr;
    }
    std::vector<char> resfile;
    if (!ReadUtf8Arg(env, args[0], resfile)) {
        return nullptr;
    }
    std::vector<char> cache(1, '\0');
    if (argc >= 2 && args[1] != nullptr && IsNapiString(env, args[1])) {
        if (!ReadUtf8Arg(env, args[1], cache)) {
            cache.assign(1, '\0');
        }
    }
    perspective_crop_set_ohos_dirs(static_cast<void *>(resfile.data()), static_cast<void *>(cache.data()));
    return nullptr;
}

static napi_value GetMagnifierBlit(napi_env env, napi_callback_info info)
{
    (void)info;
    // 96×96×4 上限；略留余量
    constexpr int kMaxRgba = 96 * 96 * 4 * 2;
    constexpr int kMaxPts = 8;
    std::vector<uint8_t> rgba(static_cast<size_t>(kMaxRgba));
    float pts[kMaxPts] = {0};
    int generation = 0;
    int width = 0;
    int height = 0;
    int rgbaLen = kMaxRgba;
    float borderWidth = 2.f;
    int borderColor = static_cast<int>(0xFFFFFFFFu);
    int showCross = 0;
    int crossColor = static_cast<int>(0xFFFFFFFFu);
    float crossLineWidth = 3.f;
    float crossLineLength = 15.f;
    int showCropLineMirror = 1;
    int cropLineColor = static_cast<int>(0xFFFF3B30u);
    float cropLineWidth = 2.f;
    float touchViewX = 0.f;
    float touchViewY = 0.f;
    int viewPointsLen = kMaxPts;

    int hasPixels = perspective_crop_magnifier_blit_snapshot(
        &generation, &width, &height, rgba.data(), &rgbaLen, &borderWidth, &borderColor, &showCross,
        &crossColor, &crossLineWidth, &crossLineLength, &showCropLineMirror, &cropLineColor,
        &cropLineWidth, &touchViewX, &touchViewY, pts, &viewPointsLen);

    napi_value result = nullptr;
    if (!NapiOk(napi_create_object(env, &result)) || result == nullptr) {
        return nullptr;
    }
    SetNamedInt(env, result, "generation", generation);
    SetNamedInt(env, result, "width", width);
    SetNamedInt(env, result, "height", height);
    SetNamedBool(env, result, "hasPixels", hasPixels == 1 && rgbaLen > 0);
    SetNamedDouble(env, result, "borderWidth", borderWidth);
    SetNamedInt(env, result, "borderColor", borderColor);
    SetNamedBool(env, result, "showCross", showCross != 0);
    SetNamedInt(env, result, "crossColor", crossColor);
    SetNamedDouble(env, result, "crossLineWidth", crossLineWidth);
    SetNamedDouble(env, result, "crossLineLength", crossLineLength);
    SetNamedBool(env, result, "showCropLineMirror", showCropLineMirror != 0);
    SetNamedInt(env, result, "cropLineColor", cropLineColor);
    SetNamedDouble(env, result, "cropLineWidth", cropLineWidth);
    SetNamedDouble(env, result, "touchViewX", touchViewX);
    SetNamedDouble(env, result, "touchViewY", touchViewY);

    const int ptsCount = viewPointsLen < 0 ? 0 : (viewPointsLen > kMaxPts ? kMaxPts : viewPointsLen);
    napi_value pointsArr = nullptr;
    if (!NapiOk(napi_create_array_with_length(env, static_cast<size_t>(ptsCount), &pointsArr)) ||
        pointsArr == nullptr) {
        return result;
    }
    for (int i = 0; i < ptsCount; ++i) {
        napi_value fv = nullptr;
        if (!NapiOk(napi_create_double(env, pts[i], &fv))) {
            continue;
        }
        napi_set_element(env, pointsArr, static_cast<uint32_t>(i), fv);
    }
    napi_set_named_property(env, result, "viewPointsXy", pointsArr);

    if (hasPixels == 1 && rgbaLen > 0) {
        void *data = nullptr;
        napi_value ab = nullptr;
        if (NapiOk(napi_create_arraybuffer(env, static_cast<size_t>(rgbaLen), &data, &ab)) &&
            data != nullptr && ab != nullptr) {
            memcpy(data, rgba.data(), static_cast<size_t>(rgbaLen));
            napi_set_named_property(env, result, "pixels", ab);
        } else {
            napi_value nullVal = nullptr;
            napi_get_null(env, &nullVal);
            napi_set_named_property(env, result, "pixels", nullVal);
            SetNamedBool(env, result, "hasPixels", false);
        }
    } else {
        napi_value nullVal = nullptr;
        napi_get_null(env, &nullVal);
        napi_set_named_property(env, result, "pixels", nullVal);
    }
    return result;
}

EXTERN_C_START
static napi_value Init(napi_env env, napi_value exports)
{
    napi_property_descriptor desc[] = {
        {"initKuikly", nullptr, InitKuikly, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"setPerspectiveCropResfileDir", nullptr, SetPerspectiveCropResfileDir, nullptr, nullptr, nullptr,
         napi_default, nullptr},
        {"setPerspectiveCropOhosDirs", nullptr, SetPerspectiveCropOhosDirs, nullptr, nullptr, nullptr, napi_default,
         nullptr},
        {"getMagnifierBlit", nullptr, GetMagnifierBlit, nullptr, nullptr, nullptr, napi_default, nullptr},
    };
    napi_define_properties(env, exports, sizeof(desc) / sizeof(desc[0]), desc);
    return exports;
}
EXTERN_C_END

static napi_module demoModule = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = Init,
    .nm_modname = "entry",
    .nm_priv = ((void*)0),
    .reserved = { 0 },
};

extern "C" __attribute__((constructor)) void RegisterEntryModule(void)
{
    napi_module_register(&demoModule);
}
