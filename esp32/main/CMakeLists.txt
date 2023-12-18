idf_component_register(
    SRCS "app_main.c" "nt_wifi.c"
    REQUIRES nvs_flash esp_wifi esp_websocket_client esp_event driver
    INCLUDE_DIRS "."
)
