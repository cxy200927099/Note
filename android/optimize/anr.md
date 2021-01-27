# android anr定位及分析

## 定位
发生ANR时通常会有以下日志显示
```log
2021-01-08 21:32:53.699 3987-7000/? I/QUALITY-TOTAL: exp: anr
2021-01-08 21:32:53.769 1879-2814/? D/ActivityManager:  ANR post Runnable for ProcessRecord{aa162de 22442:com.xunlei.tdlive/u0a317} to deal with anr happend at 298035771@#@22442
2021-01-08 21:32:53.769 1879-2814/? D/ActivityManager:  ANR caller(2) = com.android.server.am.ActivityManagerService$LocalService.inputDispatchingTimedOut:19906 com.android.server.wm.ActivityRecord.keyDispatchingTimedOut:2678 com.android.server.wm.AppWindowToken.keyDispatchingTimedOut:1999 com.android.server.wm.InputManagerCallback.notifyANR:111 com.android.server.input.InputManagerService.notifyANR:1828 <bottom of call stack> <bottom of call stack> <bottom of call stack> 
2021-01-08 21:32:53.835 1879-23343/? I/ActivityManager: Skipping duplicate ANR: ProcessRecord{aa162de 22442:com.xunlei.tdlive/u0a317} Input dispatching timed out (Waiting to send non-key event because the touched window has not finished processing certain input events that were delivered to it over 500.0ms ago.  Wait queue length: 15.  Wait queue head age: 10645.3ms.)
2021-01-08 21:32:59.613 1879-23308/? E/ActivityManager: ANR in com.xunlei.tdlive (com.xunlei.tdlive/.activity.LivePlay1v1Activity)
    PID: 22442
    Reason: Input dispatching timed out (Waiting to send non-key event because the touched window has not finished processing certain input events that were delivered to it over 500.0ms ago.  Wait queue length: 15.  Wait queue head age: 5554.9ms.)
    Parent: com.xunlei.tdlive/.activity.LivePlay1v1Activity
    Load: 0.0 / 0.0 / 0.0
    CPU usage from 5075ms to 10848ms later (2021-01-08 21:32:53.770 to 2021-01-08 21:32:59.543):
      158% 960/android.hardware.camera.provider@2.4-service_64: 141% user + 16% kernel / faults: 28589 minor
      31% 22442/com.xunlei.tdlive: 23% user + 8.6% kernel / faults: 43 minor
      11% 1079/surfaceflinger: 7.2% user + 3.9% kernel / faults: 472 minor 25 major
      2.6% 1454/media.codec: 1.8% user + 0.8% kernel / faults: 15098 minor 50 major
      7.1% 1354/cameraserver: 3.8% user + 3.2% kernel / faults: 2208 minor 15 major
      7% 1879/system_server: 3.2% user + 3.8% kernel / faults: 525 minor
      0% 1486/media.swcodec: 0% user + 0% kernel / faults: 5414 minor 105 major
      3.1% 969/android.hardware.graphics.composer@2.3-service: 1.3% user + 1.7% kernel / faults: 8 minor
      2.5% 4115/com.android.phone: 1.3% user + 1.2% kernel / faults: 1886 minor 286 major
      0.5% 1415/media.extractor: 0.1% user + 0.3% kernel / faults: 4091 minor 52 major
      2% 21112/kworker/u16:5: 0% user + 2% kernel
      0.4% 824/zygote64: 0% user + 0.3% kernel / faults: 300 minor 4 major
      1.7% 4801/irq/102-1436400: 0% user + 1.7% kernel
      1.7% 14648/kworker/u16:8: 0% user + 1.7% kernel
      1.7% 21973/kworker/u16:13: 0% user + 1.7% kernel
      0% 14333/kworker/u16:4: 0% user + 0% kernel
      1.5% 23327/kworker/u16:3: 0% user + 1.5% kernel
      1.3% 4728/sugov:6: 0% user + 1.3% kernel
      1.2% 239/crtc_commit:102: 0% user + 1.2% kernel
      1.2% 22800/adbd: 0.3% user + 0.8% kernel / faults: 10 minor
      1% 180/IPCRTR_lpass_sm: 0% user + 1% kernel
      0.5% 825/zygote: 0% user + 0.5% kernel / faults: 20 minor 4 major
      1% 927/lpass_IPCRTR: 0% user + 1% kernel
      1% 17313/kworker/u17:3: 0% user + 1% kernel
      0.8% 7/rcu_preempt: 0% user + 0.8% kernel
      0.8% 556/logd: 0.1% user + 0.6% kernel
      0.8% 3531/scheduler_threa: 0% user + 0.8% kernel
      0.8% 4727/sugov:0: 0% user + 0.8% kernel
      0.6% 3987/com.coloros.persist.system: 0.1% user + 0.5% kernel / faults: 78 minor 2 major
      0.6% 14654/kworker/u17:2: 0% user + 0.6% kernel
      0.1% 1/init: 0% user + 0% kernel
      0.5% 8/rcu_sched: 0% user + 0.5% kernel
      0.5% 987/android.hardware.wifi@1.0-service: 0.1% user + 0.3% kernel
      0.5% 1010/vendor.qti.hardware.display.allocator@1.0-service: 0.1% user + 0.3% kernel
      0.2% 1063/audioserver: 0% user + 0.2% kernel / faults: 52 minor 20 major
      0.5% 14650/kworker/u16:12: 0% user + 0.5% kernel
      0.3% 10/rcuop/0: 0% user + 0.3% kernel
      0.3% 240/crtc_event:102: 0% user + 0.3% kernel
      0.3% 246/kgsl_worker_thr: 0% user + 0.3% kernel
      0.1% 1074/lmkd: 0% user + 0.1% kernel
      0.1% 1438/statsd: 0% user + 0.1% kernel / faults: 23 minor 19 major
      0.3% 1445/wificond: 0% user + 0.3% kernel
      0% 1535/tombstoned: 0% user + 0% kernel / faults: 15 minor 46 major
      0.3% 10204/com.android.systemui: 0.1% user + 0.1% kernel / faults: 14 minor
      0.3% 14655/kworker/u16:14: 0% user + 0.3% kernel
      0.3% 23335/logcat: 0.1% user + 0.1% kernel
      0% 6/ksoftirqd/0: 0% user + 0% kernel
      0% 11/rcuos/0: 0% user + 0% kernel
      0% 21/rcuop/1: 0% user + 0% kernel
      0.1% 29/rcuop/2: 0% user + 0.1% kernel
      0% 30/rcuos/2: 0% user + 0% kernel
      0.1% 45/rcuop/4: 0% user + 0.1% kernel
      0% 49/migration/5: 0% user + 0% kernel
      0% 53/rcuop/5: 0% user + 0% kernel
      0% 62/rcuos/6: 0% user + 0% kernel
      0% 84/smem_native_lpa: 0% user + 0% kernel
      0.1% 85/lpass_smem_glin: 0% user + 0.1% kernel
      0% 402/psimon: 0% user + 0% kernel
      0% 479/core_ctl/0: 0% user + 0% kernel
      0% 557/servicemanager: 0% user + 0% kernel
      0% 570/vold: 0% user + 0% kernel / faults: 37 minor 18 major
      0% 641/jbd2/sda13-8: 0% user + 0% kernel
      0% 957/android.system.suspend@1.0-service: 0% user + 0% kernel
      0% 970/android.hardware.health@2.0-se
2021-01-08 21:32:59.613 1879-23308/? V/java.lang.ASSERT: copyAnr filePath = /data/anr/anr_2021-01-08-21-32-49-502
2021-01-08 21:32:59.638 1879-2131/? W/ContextImpl: Calling a method in the system process without a qualified user: android.app.ContextImpl.sendBroadcast:1070 com.android.server.am.OppoExtraActivityManagerService.setKeyLockModeNormal:56 com.android.server.am.ActivityManagerService.killAppAtUsersRequest:10163 com.android.server.am.AppErrors.handleShowAnrUi:939 com.android.server.am.ActivityManagerService$UiHandler.handleMessage:1785 
2021-01-08 21:32:59.659 1879-23440/? I/DropBoxManagerService: add tag=data_app_anr isTagEnabled=true flags=0x2
2021-01-08 21:32:59.686 3987-23441/? I/QualityProtectService: EAP_LOG:eap_log_anr
2021-01-08 21:32:59.691 1879-23440/? D/ColorEapUtils: collectFile: com.xunlei.tdlive, 8c8c959353aa41b9, data_app_anr
2021-01-08 21:32:59.692 1879-23440/? D/ColorEapUtils: copyFile /data/system/dropbox/data_app_anr@1610112779690.txt.gz to /data/oppo/coloros/eap/8c8c959353aa41b9/data_app_anr@1610112779690.txt.gz
2021-01-08 21:33:00.769 23452-23452/com.xunlei.tdlive I/CrashReport: anr changed to true
2021-01-08 21:33:00.769 23452-23452/com.xunlei.tdlive I/CrashReport: start anr monitor!
2021-01-08 21:33:00.770 23452-23452/com.xunlei.tdlive D/CrashReport: [AsyncTaskHandler] Post a normal task: com.tencent.bugly.crashreport.crash.anr.b$2
```
日志上显示ANR已经将相关日志写入 `/data/anr/anr_2021-01-08-21-32-49-502` 这个文件了，但是现在我们拿不到这个文件
adb 访问 permission denied

- adb bugreport
执行这个命令会把相关文件下载到本地目录
```bash
$ adb bugreport
/data/user_de/0/com.android.shell/files/bugreports/bugreport-PCAM00-QKQ1.190918.001-2021-01-08-21-48-17.zip: 1 file pulled, 0 skipped. 36.6 MB/s (10321596 bytes in 0.269s)
```
解压说这个目录就可以找到 `anr_2021-01-08-21-32-49-502` 这个文件了



