package com.nativeside.api.rest.constants;

public interface Constants {

  String CREATE_APP_URI = "https://onesignal.com/api/v1/apps";
  String EXPORT_CSV_URI = "https://onesignal.com/api/v1/players/csv_export?app_id=";
  String PUSH_URI = "https://onesignal.com/api/v1/notifications";

  String COLLECT_REVCONTENT_URI = "https://push.stackie.com/api/v2/?api_key=b59125f7d92e201feb35fcf85df485e94e58d0bd&pub_id=99073&widget_id=103774&domain=push.nativesidetest.com&user_agent=Mozilla%2F5.0%20(Windows%20NT%2010.0%3B%20WOW64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F61.0.3163.79%20Safari%2F537.36&format=json&sponsored_count=1&tracking=auto&user_ip=";
  String COLLECT_MGID_URI = "http://api.mgid.com/282540?content_type=json&src_id=SOURCEID&token=646e144572186b4e1496ef0745d092e8&ua=Mozilla%2F5.0%20(Windows%20NT%2010.0%3B%20WOW64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F61.0.3163.79%20Safari%2F537.36&ip=";

  String FILE_NAME_CSV = "users.csv";
  Integer THREAD_POOL_SIZE = 50;

}
