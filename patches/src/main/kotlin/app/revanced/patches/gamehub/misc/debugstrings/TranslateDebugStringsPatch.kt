package app.revanced.patches.gamehub.misc.debugstrings

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/** Strings set programmatically across multiple debug-related classes. */
private val smaliTranslations = mapOf(
    // SettingMainActivity — debug section menu item label
    "调试" to "Debug",

    // ServerEnv — environment display names
    "正式环境" to "Release",
    "预发布环境" to "Beta",
    "测试环境" to "Test",

    // DebugFragment — dialog / toast messages
    "请先添加Chucker依赖" to "Please add Chucker dependency first",
    "已复制" to "Copied",
    "请输入URL（例如：https://example.com）" to "Please enter URL (e.g. https://example.com)",
    "请输入有效的URL" to "Please enter a valid URL",
    "请输入HTML内容" to "Please enter HTML content",
    "请先输入内容" to "Please enter content first",
    "打开网页" to "Open Web",
    "确定" to "OK",
    "取消" to "Cancel",
    // DebugFragment — status labels (set at runtime)
    "当前环境：" to "Current Env:",
    "渠道Channel：" to "Channel:",
    "打包Flavor：" to "Build Flavor:",
    "Git SHA：" to "Git SHA:",
    "打包时间：" to "Build Time:",
    "Env：" to "Env:",
    "Build Type：release" to "Build Type: release",

    // GameLibraryActivity — test dialog buttons / labels
    "id 无效" to "Invalid ID",
    "输入steam app id" to "Enter Steam App ID",
    "确认" to "Confirm",
    "卸载所有下载的steam游戏及文件" to "Uninstall all downloaded Steam games and files",
    "检查游戏是否需要下载Steamworks" to "Check if game needs Steamworks download",
    "卸载Steamworks" to "Uninstall Steamworks",
    "计算Steam游戏容量" to "Calculate Steam game size",
    "校验已下载Steam游戏" to "Verify downloaded Steam games",
    "测试" to "Test",
    // SteamPersonalInfoFragment
    "steam游戏列表-" to "Steam game list-",
    // SteamPersonalInfoFragment$logoutAccount$1
    "退出账号成功" to "Logout successful",
    "退出账号失败: " to "Logout failed: ",
    // GameLibraryActivity$showTestDialog* lambdas
    "检查游戏是否需要下载Steamworks iNeed：" to "Check if game needs Steamworks iNeed:",
    "检查游戏是否需要下载Steamworks 耗时：" to "Check if game needs Steamworks time taken:",
    "开始卸载" to "Uninstall started",
    "卸载完成" to "Uninstall complete",
    "计算结果" to "Calculation result",
    "安装容量：" to "Install size:",
    ",下载大小：" to ", download size:",
    "关闭" to "Close",
    "卸载Steamworks 耗时：" to "Uninstall Steamworks time taken:",

    // SteamGameManager
    "initNoInfoSteamGame ， 用户未登录steam，找不到对应游戏信息" to "initNoInfoSteamGame , user not logged into Steam, game info not found",

    // SteamGameByPcEmuLaunchStrategy
    "没有游戏信息" to "No game info",
    "没有steam游戏id" to "No Steam game ID",
    "启动已下载steam游戏，不是steam64位userId，刷新一下,isSteamLogOn =" to "Launch downloaded Steam game, not steam64 userId, refreshing, isSteamLogOn =",
    "） state = " to ") state = ",
    "切换下载状态" to "Switch download state",
    // SteamGameByPcEmuLaunchStrategy$checkCanStartSteamGame$2$1$2
    "检查启动游戏结果失败 " to "Check launch game result failed ",
    // SteamGameByPcEmuLaunchStrategy$execute$5
    "没有下载过，开始下载新的任务" to "Not downloaded, starting new download task",
    "取消下载" to "Cancel download",
    "steam限免游戏入库" to "Steam free-to-play game library",
    "steam限免游戏入库 失败" to "Steam free-to-play game library failed",
    // SteamGameByPcEmuLaunchStrategy$execute$3$1
    "Steam游戏启动，游戏库信息获取失败" to "Steam game launch, game library info fetch failed",
    "启动成功 ,exePath = " to "Launch successful, exePath = ",
    // SteamGameByPcEmuLaunchStrategy$execute$2
    "启动成功" to "Launch successful",

    // MobilePlayLaunchStrategy$launch$1
    "手游模式 gameId = " to "Mobile game mode gameId = ",
    " 启动类型 = " to " launch type = ",

    // PlayPcEmuGameDeepLinkHandler
    "回调结果 " to "Callback result ",
    "启动PC模拟器游戏（steam） - 回调结果 " to "Launch PC emulator game (steam) - Callback result ",
    "启动pc模拟器游戏失败，没有游戏id" to "Launch PC emulator game failed, no game ID",
    "启动pc模拟器游戏失败，获取游戏信息失败，不继续处理，停留在主页" to "Launch PC emulator game failed, game info fetch failed, stopped at home page",

    // GameAppLauncherHelper — launch result logging
    "callback 启动游戏成功, launchType: " to "callback launch game success, launchType: ",
    "callback 启动游戏失败, launchType: " to "callback launch game failed, launchType: ",
    "启动pc游戏失败，startupParams: " to "Launch PC game failed, startupParams: ",
    "启动pc_link游戏结果 " to "Launch pc_link game result ",
    "启动游戏失败，launchType: " to "Launch game failed, launchType: ",
    "启动手游失败 UnInstalled，launchType: " to "Launch mobile game failed UnInstalled, launchType: ",
    "启动游戏失败" to "Launch game failed",
    "启动游戏成功, launchType: " to "Launch game success, launchType: ",
    // GameAppLauncherHelper$launch$1
    "启动游戏失败，gameData: " to "Launch game failed, gameData: ",

    // SteamChangeAccountViewModel
    "切换steam账户异常" to "Switch Steam account exception",
    "删除steam账户异常" to "Delete Steam account exception",
    "获取steam全部账户异常" to "Get all Steam accounts exception",
    // SteamChangeAccountViewModel$changeAccount$1
    "相同账号" to "Same account",
    "切换账户的登录结果 " to "Account switch login result ",

    // SteamGamePriceApi$requestGamePrices$2
    "steam游戏价格请求开始 language: " to "Steam game price request started, language: ",
    "steam游戏价格请求结果 为空" to "Steam game price request result is empty",
    "steam游戏价格请求结果 size: " to "Steam game price request result size: ",
    // SteamGamePriceApi
    "价格解析异常 " to "Price parse exception ",
    "价格解析异常, " to "Price parse exception, ",

    // PcGamePresetConfigRepository
    " 需要下载？ " to " needs download? ",
    "通用配置有用户选中的steam版本，不需要添加兜底" to "General config has user-selected Steam version, no need to add fallback",
    "通用配置没有有用户选中的steam版本，添加一个进行兜底" to "General config has no user-selected Steam version, adding one as fallback",
    "组件列表没有拉取到steam 组件信息" to "Component list has no Steam component info",
    "本地没有用户选择的组件 (" to "No locally selected component (",
    ") 信息 ,下载推荐" to ") info, downloading recommended",
    "本地用户选择的组件 (" to "Locally selected component (",
    ") 需要重新下载" to ") needs re-download",
    ") 已下载，无需重新下载" to ") already downloaded, no re-download needed",
    "用户没有选择 组件 type " to "User has not selected component type ",
    ",检测是否需要下载推荐的" to ", checking if recommended download is needed",

    // WinEmuServiceImpl$launchContainer$1$1$7 — debug log header and fields
    "PC模拟器启动参数:—————————————————————————— \n语言 = " to "PC emulator launch params:—————————————————————————— \nlanguage = ",
    " \n启动路径 = " to " \nstart path = ",
    " \nGPU驱动 = " to " \nGPU driver = ",
    " \n基础容器路径 = " to " \nbase container path = ",
    " \n启动参数 = " to " \nlaunch args = ",
    " \n启动方式 = " to " \nlaunch method = ",
    " \n环境变量 = " to " \nenv vars = ",
    " \nDxvk路径 = " to " \nDxvk path = ",
    " \nvkD3D路径 = " to " \nvkD3D path = ",
    " \nbox64转译器路径 = " to " \nbox64 translator path = ",
    " \nfex转译器路径 = " to " \nfex translator path = ",
    "\n分辨率 = " to "\nresolution = ",
    "\nsteam客户端 = fake=" to "\nsteam client = fake=",
    "\nsteam用户信息 = " to "\nsteam user info = ",
    " \nsteam游戏信息 = " to " \nsteam game info = ",
    // WinEmuServiceImpl$launchContainer$1 — component path debug
    "dxvk路径 " to "DXVK path ",
    "vkd3d路径 " to "VKD3D path ",
    "转译器下载路径 " to "Translator download path ",
    "转译器使用内置" to "Translator using built-in",
    "转译器已安装(fex) " to "Translator installed (fex) ",
    "转译器使用内置,当前选中的并不是fex " to "Translator using built-in, current selection is not fex ",
    "转译器已安装(box) " to "Translator installed (box) ",
    "gpu已安装 " to "GPU installed ",
    // WinEmuServiceImpl — virtual container management
    "对应游戏的容器名为空" to "Corresponding game container name is empty",
    "对应容器实例不存在" to "Corresponding container instance does not exist",
    "创建新的虚拟容器-成功 " to "Create new virtual container - success ",
    "创建新的虚拟容器-失败 " to "Create new virtual container - failed ",
    "对应基础容器路径是否变化=" to "Base container path changed=",
    " 虚拟容器路径 " to " virtual container path ",
    "虚拟容器已存在并且基础容器没有变化" to "Virtual container exists and base container unchanged",
    "虚拟容器的base需要更换" to "Virtual container base needs replacement",
    "更换虚拟容器的base容器成功" to "Replace virtual container base container success",
    "更换虚拟容器的base容器失败" to "Replace virtual container base container failed",
    "虚拟容器不存在" to "Virtual container does not exist",

    // PcGameSettingDataHelper$initDefaultSetting$1
    "容器默认值数据解析失败 " to "Container default value parse failed ",
    "gpu默认值数据解析失败 " to "GPU default value parse failed ",
    "dxvk默认值数据解析失败 " to "DXVK default value parse failed ",
    "vkd3d默认值数据解析失败 " to "VKD3D default value parse failed ",
    "转译器默认值数据解析失败 " to "Translator default value parse failed ",
    "steam客户端默认值数据解析失败 " to "Steam client default value parse failed ",
    // PcGameSettingDataHelper — component download status
    "checkAndDownload - 组件" to "checkAndDownload - component",
    "checkAndDownload - 检查" to "checkAndDownload - check",
    "组件下载状态: " to "Component download status: ",
    "未下载，需要下载" to "Not downloaded, needs download",
    "下载失败 " to "Download failed ",
    "组件下载结果:" to "Component download result:",

    // WineActivity
    "链接已复制到剪贴板" to "Link copied to clipboard",
    "wine页面 " to "Wine page ",
    " - 是否steam游戏=" to " - is Steam game=",
    " - 可使用steam客户端=" to " - has Steam client=",
    "winuiBridge attach失败 " to "winuiBridge attach failed ",
    // WebActivity$initView$2 — SSL certificate dialog
    "ssl证书验证失败" to "SSL certificate verification failed",
    "继续" to "Continue",

    // UninstallGameHelper$uninstallGame$2
    "uninstallGame 卸载游戏" to "uninstallGame uninstall game",
    "未识别的游戏来源，不进行卸载操作" to "Unrecognized game source, skipping uninstall",

    // GameLibraryRepository$loadRecentGameList$2
    "获取最近游戏列表数据失败 " to "Get recent game list data failed ",
    // GameLibraryRepository
    "saveIcon2LocalIfNeed ，need2generate = " to "saveIcon2LocalIfNeed , need2generate = ",
    "setIcon ，is null = " to "setIcon , is null = ",
    ")，is success = " to "), is success = ",
    "已经存在该游戏，且不替换" to "Game already exists, not replacing",
    "，validId = " to ", validId = ",

    // GamesFragment, GamesPageFragment
    "忽略重复内容的刷新" to "Ignoring refresh of duplicate content",
    // GamesPageFragment
    "删除的元素 " to "Deleted element ",
    " , 获取焦点的元素 " to " , focused element ",

    // GameLibraryRecentGameHolder$onViewCreated$1
    "最近游戏下载状态 " to "Recent game download state ",

    // LocalGamesViewModel
    "批量获取游戏类型失败 " to "Batch get game type failed ",
    // LocalGamesViewModel$loadGameCategories$1
    "解析批量获取游戏类型json失败 " to "Parse batch get game type JSON failed ",

    // MobileManageVM
    "管理游戏页获取本地数据整合列表失败" to "Game management page failed to get local data integration list",
    "加入游戏库异常" to "Add to game library exception",

    // SteamGamesViewModel$loadCloudGameState$1
    "解析批量获取云游戏id json失败 " to "Parse batch get cloud game ID JSON failed ",

    // MobileManagerDataHelper$fetchGameInfoByServer$1
    "fetchGameInfoByServer 开始请求" to "fetchGameInfoByServer request started",
    "fetchGameInfoByServer 请求结束 " to "fetchGameInfoByServer request ended ",
    // MobileManagerDataHelper
    "解析手游线上数据失败" to "Parse mobile game online data failed",

    // ImportPcGameHelper and lambdas
    "清单文件存在，开始删除 " to "List file exists, starting deletion ",
    "删除清单文件完毕 " to "List file deletion complete ",
    "导入游戏处理发生异常 " to "Import game processing exception ",
    "导入未匹配到的游戏-" to "Importing unmatched game-",
    "成功" to "Success",
    "无游戏可导入" to "No games to import",
    "读取到游戏-内 " to "Reading game - contains ",
    "开始解压 获取解压包路径 " to "Starting decompression, getting archive path ",
    "解压成功 " to "Decompression succeeded ",
    "解压失败 " to "Decompression failed ",
    "解压进度 " to "Decompression progress ",
    "未知错误: " to "Unknown error: ",
    "错误: 不支持的加密方法 - " to "Error: unsupported encryption method - ",
    "错误: 文件不存在 - " to "Error: file not found - ",
    "错误: 未知的压缩方法 - " to "Error: unknown compression method - ",
    "错误: 校验和不匹配 - " to "Error: checksum mismatch - ",
    "错误: 任务被取消 - " to "Error: task cancelled - ",
    "错误: 密码不正确 - " to "Error: incorrect password - ",
    "存入游戏路径" to "Save game path",
    "解析本地pc游戏json失败" to "Parse local PC game JSON failed",
    "拷贝steam游戏信息文件 " to "Copying Steam game info file ",
    "拷贝steam游戏信息文件成功" to "Steam game info file copy successful",
    "接口获取到游戏 " to "Interface fetched game ",
    "读取steam游戏失败，目录不存在或不是目录" to "Read Steam game failed, directory doesn't exist or is not a directory",
    "标记文件存在，开始删除 " to "Marker file exists, starting deletion ",
    "-游戏文件夹存在，开始删除 " to "-Game folder exists, starting deletion ",
    "删除游戏文件夹完毕" to "Game folder deletion complete",
    "删除游戏文件夹异常 " to "Game folder deletion exception ",
    "全部导入完成 " to "All imports complete ",
    "导入匹配到的游戏-" to "Importing matched game-",
    "读取到游戏 是否存在" to "Read game, checking if exists",
    "有游戏需要导入" to "Games need to be imported",
    "判断是否存在需要导入游戏异常 " to "Exception checking if games need import ",

    // GameDetailActivity — bottom action buttons
    "下载历史版本" to "History",
    "启动游戏" to "Play",
    "文件校验" to "Verify",
    // FileVerify and lambdas — verification status / progress messages
    "开始校验" to "Verifying",
    "开始校验 " to "Start verify ",
    // FileVerify$verify$1$1$1$1
    "开始校验 depotId: " to "Start verify depotId: ",
    "开始校验 depotManifestId: " to "Start verify depotManifestId: ",
    // FileVerify$verify$1
    "校验成功" to "Verification passed",
    "校验成功 " to "Verification passed ",
    "校验失败 " to "Verification failed ",
    "校验失败！" to "Verification failed!",
    "校验失败！发现 " to "Verification failed! Found ",
    " 个错误" to " errors",
    " 个错误:\n" to " errors:\n",
    "：发现 " to ": found ",
    "发现 " to "Found ",
    // FileVerify
    " 校验失败：" to " verify failed:",
    // FileVerify$verify$1$1$1$1$1$1 — per-file verification detail
    " 校验失败: " to " verify failed: ",
    " 块校验失败 ( offset=" to " chunk verify failed (offset=",
    " 块超出边界 (offset=" to " chunk out of bounds (offset=",
    " 文件不存在" to " file not found",
    " 文件大小不匹配 (预期=" to " file size mismatch (expected=",
    " 文件大小应为0但实际为" to " file size should be 0 but actual is",
    " 目录不存在" to " directory not found",
    "), 预期: " to "), expected: ",
    ", 实际: " to ", actual: ",
    ", 实际=" to ", actual=",
    // SteamGameUpdateDownloader — depot-level verification failures
    "文件校验失败, depotFile: " to "File verify failed, depotFile: ",
    // SteamGameUpdateDownloader$downloadFileChunks$chunkDownloadJobs$1$1
    "文件块校验失败: " to "File chunk verify failed: ",
    "文件块下载完成: " to "File chunk download complete: ",
    // SteamGameUpdateDownloader — download / decompress logs
    "创建目录失败" to "Create directory failed",
    "所有文件下载完成" to "All files downloaded",
    "所有文件解压完成" to "All files decompressed",
    "部分文件处理失败，下载失败: " to "Some files failed, download failed: ",
    ", 解压失败: " to ", decompression failed: ",
    // SteamGameUpdateDownloader$downloadAndDecompressFiles$downloadJobs$1$1$1
    "文件下载完成: " to "File download complete: ",
    "文件下载失败: " to "File download failed: ",
    // SteamGameUpdateDownloader$downloadAndDecompressFiles$decompressWorkers$1$1
    "解压worker-" to "Decompress worker-",
    " 启动" to " started",
    " 完成文件: " to " completed file: ",
    " 失败: " to " failed: ",
    " 退出" to " exited",

    // GameDetailActivity and lambdas — historical version UI
    "开始获取历史版本\u2026\u2026" to "Fetching historical versions\u2026",
    "下载历史版本失败" to "Download history version failed",
    "没有找到历史版本" to "No historical versions found",
    "选择历史版本" to "Select Historical Version",
    "选中历史版本: " to "Selected version: ",
    "\n是否确认下载此版本？" to "\nConfirm download of this version?",
    "确认下载" to "Confirm Download",
    "版本下载完成" to "Version download complete",
    "获取历史版本失败" to "Get historical versions failed",

    // GameDetailActivity — download status / progress
    "下载中: " to "Downloading: ",
    "下载失败" to "Download failed",
    "下载失败: " to "Download failed: ",
    "下载已暂停" to "Download paused",
    "下载异常" to "Download error",
    "下载异常: " to "Download error: ",
    "下载大小: " to "Download size: ",
    "准备下载..." to "Preparing download...",
    "开始下载..." to "Starting download...",
    "开始下载指定版本 appId: " to "Starting download of specified version appId: ",
    "正在计算下载大小..." to "Calculating download size...",
    "获取下载大小失败" to "Get download size failed",
    "获取下载大小失败: " to "Get download size failed: ",
    "已经是目标版本，无需下载" to "Already at target version, no download needed",
    "暂停" to "Pause",
    "操作失败: " to "Operation failed: ",

    // GameDetailActivity$initObserver$6, WebActivity$initObserver$1 — download observer debug
    "  下载文件大小 :" to "  download file size :",
    " 下载拓展信息 :" to " download extended info :",
    " 下载状态 :" to " download status :",
    " 下载进度 :" to " download progress :",
    " 当前下载大小 :" to " current download size :",
    "下载信息 名称 :" to "download info name :",
    // GameDetailActivity — debug / log messages
    " 没有找到本地游戏信息" to " local game info not found",
    // GameDetailSettingMenu
    " ， gameType = " to " , gameType = ",
    // GameDetailActivity — debug / log messages (continued)
    "1407回调结果 " to "1407 callback result ",
    "启动pc游戏结果 " to "Launch PC game result ",
    "存数据库 " to "Save to DB ",
    "解压信息 " to "Decompression info ",
    "滚动 onPause " to "Scroll onPause ",
    "滚动 onScrollStateChanged " to "Scroll onScrollStateChanged ",
    "详情页 destroy崩溃 " to "Detail page destroy crash ",
    "详情页-channel= " to "Detail page-channel= ",
    "详情页-startup= " to "Detail page-startup= ",
    "遗漏停止滑动事件，记录下次补发" to "Missed stop scroll event, recording for next dispatch",
    "triggerJoinGameLibrary 加入游戏库：" to "triggerJoinGameLibrary add to game library:",

    // IGameHeadEntityDecorator$Companion — launch type resolution
    "最终设置的当前启动方式为：" to "Final launch method set to:",
    "有pc相关的启动方式，且lastLaunchType 不为空，上一次以云游方式启动，设置为pc启动方式 hasDemo = " to "Has PC launch method, lastLaunchType not empty, last was cloud gaming, set to PC launch hasDemo = ",
    "有pc相关的启动方式，且lastLaunchType 不为空，非云游，恢复上一次的启动方式 " to "Has PC launch method, lastLaunchType not empty, not cloud, restoring last launch method ",
    "有pc相关的启动方式，且lastLaunchType 为空，设置为pc启动方式（默认试玩版） hasDemo = " to "Has PC launch method, lastLaunchType empty, set to PC launch (default demo) hasDemo = ",
    "没有pc模拟器相关的启动方式，恢复上一次？" to "No PC emulator launch method, restore last?",

    // SteamLoginViewModel
    "输入校验码 " to "Enter verification code ",
    // SteamLoginViewModel$authenticator$1
    "等待手机确定" to "Waiting for phone confirmation",
    // SteamLoginViewModel$qrLoginLoop$1$1
    "循环-调用authViaQRCode " to "Loop - calling authViaQRCode ",
    // SteamLoginViewModel$realQrLogin$2
    "扫码成功  accountName: " to "QR scan success  accountName: ",
    "查扫码结果1 " to "Check QR scan result 1 ",
    "查扫码结束" to "QR scan check ended",
    // SteamLoginActivity
    "获取到二维码 " to "Got QR code ",
    "二维码准备 " to "QR code ready ",
    // SteamLoginActivity$initObserver$9
    "onLoggedOn 登录超时，当前登录请求已失效" to "onLoggedOn login timed out, current login request expired",

    // AccountSettingFragment — user-visible settings labels
    "编辑游戏用户名" to "Edit game username",
    "管理订阅" to "Manage subscription",
    "订阅计划" to "Subscription plan",

    // BaseGamePadConfig — controller button labels
    "LB键" to "LB Button",
    "RB键" to "RB Button",
    "LT键" to "LT Button",
    "RT键" to "RT Button",
    "A键" to "A Button",
    "B键" to "B Button",
    "X键" to "X Button",
    "Y键" to "Y Button",
    "十字键-上" to "D-Pad Up",
    "十字键-下" to "D-Pad Down",
    "十字键-左" to "D-Pad Left",
    "十字键-右" to "D-Pad Right",
    "L3键" to "L3 Button",
    "R3键" to "R3 Button",
    "L4键" to "L4 Button",
    "R4键" to "R4 Button",
    "START键" to "START Button",
    "SELECT键" to "SELECT Button",
    "NULL键" to "NULL Button",
    "宏" to "Macro",

    // CaptureMonitorFragment — screen recording
    "正在录屏" to "Recording screen",
    "延迟处理" to "Delayed processing",
    "防抖" to "Debounce",
    "用户授权成功" to "User authorization granted",
    "用户拒绝授权" to "User authorization denied",
)

/** Static android:text values hardcoded in the debug layout XML. */
private val layoutTranslations = mapOf(
    "打包Flavor：" to "Build Flavor:",
    "Git SHA：" to "Git SHA:",
    "打包时间：" to "Build Time:",
    "渠道Channel：" to "Channel:",
    "Build Type：" to "Build Type:",
    "环境Env：" to "Env:",
    "当前环境：" to "Current Env:",
    "切至【测试环境】" to "Switch to [Test]",
    "切至【预发布环境】" to "Switch to [Beta]",
    "切至【正式环境】" to "Switch to [Release]",
    "打开网页：" to "Open Web:",
    "应用内打开HTML网页" to "Open HTML in-app",
    "应用内打开URL网页" to "Open URL in-app",
    "打开HTTP日志" to "Open HTTP Log",
    "测试功能" to "Test Features",
    // GameLibraryActivity tvTest button — opens the Steamworks test/debug dialog
    "测试操作" to "Test Operations",
    // WebActivity refresh button
    "刷新" to "Reload",
)

private val debugStringsLayoutPatch = resourcePatch {
    execute {
        for (layoutFile in listOf(
            "res/layout/llauncher_fragment_setting_debug.xml",
            "res/layout/game_activity_game_library_main.xml",
            "res/layout/winemu_activity_game_library_main.xml",
            "res/layout/comm_activity_web.xml",
        )) {
            document(layoutFile).use { dom ->
                val allElements = dom.getElementsByTagName("*")
                for (i in 0 until allElements.length) {
                    val element = allElements.item(i) as? org.w3c.dom.Element ?: continue
                    val text = element.getAttribute("android:text").takeIf { it.isNotEmpty() } ?: continue
                    val translation = layoutTranslations[text] ?: continue
                    element.setAttribute("android:text", translation)
                }
            }
        }
    }
}

@Suppress("unused")
val translateDebugStringsPatch = bytecodePatch(
    name = "Translate debug strings",
    description = "Translates Chinese strings in debug-related screens and log messages to English.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))
    dependsOn(debugStringsLayoutPatch)

    execute {
        // Scan every class in the dex for matching const-string instructions.
        // Only classes that actually contain at least one map entry are proxied for mutation.
        classes
            .filter { classDef ->
                classDef.methods.any { method ->
                    method.implementation?.instructions?.any { instr ->
                        instr.opcode == Opcode.CONST_STRING &&
                            smaliTranslations.containsKey(
                                ((instr as? ReferenceInstruction)?.reference as? StringReference)?.string,
                            )
                    } == true
                }
            }
            .forEach { classDef ->
                val mutableClass = proxy(classDef).mutableClass
                mutableClass.methods.forEach { method ->
                    val instructions = method.implementation?.instructions ?: return@forEach
                    // Iterate in reverse so index shifts from prior replacements don't matter.
                    for (idx in instructions.indices.reversed()) {
                        val instruction = instructions[idx]
                        if (instruction.opcode != Opcode.CONST_STRING) continue
                        val str = instruction.getReference<StringReference>()?.string ?: continue
                        val translation = smaliTranslations[str] ?: continue
                        val escaped = translation
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t")
                        val reg = (instruction as OneRegisterInstruction).registerA
                        method.replaceInstruction(idx, "const-string v$reg, \"$escaped\"")
                    }
                }
            }
    }
}
