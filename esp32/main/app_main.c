#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "nvs_flash.h"
#include "esp_log.h"
#include "driver/gpio.h"
#include "esp_log.h"
#include "esp_websocket_client.h"
#include <inttypes.h>
#include <string.h>
#include "config.h"
#include "nt_wifi.h"

static const char *TAG = "MAIN";
static const char *TAG_WS = "WebSocket";

#define RELAY_PIN (gpio_num_t) GPIO_NUM_4
#define MESSAGE_ON "ON"
#define MESSAGE_OFF "OFF"

void nt_websocket_event_handler(void* handler_args, esp_event_base_t base, int32_t event_id, void* event_data) 
{
    ESP_LOGI(TAG_WS, "Received websocket event: %s %" PRId32 " ", base, event_id);
    switch (event_id) {
        case WEBSOCKET_EVENT_CLOSED:
        case WEBSOCKET_EVENT_ERROR:
        case WEBSOCKET_EVENT_DISCONNECTED:
            ESP_LOGI(TAG_WS, "Disonnected or error");
            gpio_set_level(RELAY_PIN, 0);
            break;
        case WEBSOCKET_EVENT_CONNECTED:
            ESP_LOGI(TAG_WS, "Connected");
            gpio_set_level(RELAY_PIN, 0);
            break;
        case WEBSOCKET_EVENT_DATA:
            esp_websocket_event_data_t* data = (esp_websocket_event_data_t*)event_data;
            char *message = (char *)malloc(data->data_len + 1);
            if (message == NULL) {
                return;
            }
            memcpy(message, data->data_ptr, data->data_len);
            message[data->data_len] = '\0';
            ESP_LOGI(TAG_WS, "Received message %s len: %d", message, data->data_len);
            if (strcmp( MESSAGE_ON, message ) == 0) {
                gpio_set_level(RELAY_PIN, 1);
            } else if (strcmp( MESSAGE_OFF, message ) == 0) {
                gpio_set_level(RELAY_PIN, 0);
            } else {
                ESP_LOGI(TAG_WS, "WebSocket Unsuported message %s", message);
            }
            free(message);
            break;
        default: 
            gpio_set_level(RELAY_PIN, 0);
            break;
    }
}

void nt_websocket_connect()
{
    ESP_LOGI(TAG, "WebSockets Connect %s:%d", WS_HOST, WS_PORT);
    const esp_websocket_client_config_t ws_cfg = {
        .uri = WS_HOST,
        .port = WS_PORT,
        .reconnect_timeout_ms = 10000,
        .network_timeout_ms = 10000,
    };

    esp_websocket_client_handle_t client = esp_websocket_client_init(&ws_cfg);
    esp_websocket_register_events(client, WEBSOCKET_EVENT_ANY, &nt_websocket_event_handler, (void*)client);
    esp_websocket_client_start(client);
}

void app_main(void)
{
    // Initialize NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }

    ESP_ERROR_CHECK(ret);

    gpio_set_direction(RELAY_PIN, GPIO_MODE_OUTPUT);
    nt_wifi_connect();
    nt_websocket_connect();
}
