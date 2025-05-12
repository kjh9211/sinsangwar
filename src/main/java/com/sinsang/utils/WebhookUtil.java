package com.sinsang.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class WebhookUtil {

    private static final FileConfiguration config = com.sinsang.SinsangWarPlugin.getInstance().getConfig();

    public static void sendError(String message) {
        sendWebhook(config.getString("webhooks.errorWebhook"), "오류 발생: " + message);
    }

    public static void sendCaptureWebhook(String playerName, String sinsangName) {
        String message = playerName + " 플레이어가 " + sinsangName + " 신상을 점령했습니다!";
        sendWebhook(config.getString("webhooks.captureWebhook"), message);
    }

    public static void sendFinalResultWebhook() {
        StringBuilder resultMessage = new StringBuilder("최종 점령 결과:\n");
        for (Player player : Bukkit.getOnlinePlayers()) {
            resultMessage.append(player.getName()).append(" - ").append(getCapturedSinsang(player)).append("\n");
        }
        sendWebhook(config.getString("webhooks.finalResultWebhook"), resultMessage.toString());
    }

    private static String getCapturedSinsang(Player player) {
        // 여기서 각 플레이어가 점령한 신상 목록을 반환할 수 있어
        // 예시로 플레이어 이름만 리턴함.
        return player.getName(); // 이 부분은 실제 구현에 맞게 수정
    }

    private static void sendWebhook(String webhookUrl, String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("content", message);

            URL url = new URL(webhookUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode != 204) {
                Bukkit.getLogger().warning("웹훅 전송 실패: " + responseCode);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("웹훅 전송 중 오류 발생: " + e.getMessage());
        }
    }
}
