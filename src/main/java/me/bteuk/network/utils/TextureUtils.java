package me.bteuk.network.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextureUtils {

    private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("https?://.+(?<texture>\\w{64})\"");

    public static String getTexture(PlayerProfile profile) {

        String textureProperty = getTextureProperty(profile.getProperties());

        return getTexture(textureProperty);

    }

    public static String getTexture(String textureProperty) {

        if (textureProperty != null) {

            byte[] decodedBytes = Base64.getDecoder().decode(textureProperty);
            String textureData = new String(decodedBytes);

            Matcher matcher = TEXTURE_URL_PATTERN.matcher(textureData);
            if (matcher.find()) return matcher.group("texture");
        }

        return null;
    }

    public static String getTextureProperty(Set<ProfileProperty> propertyMap) {

        for (ProfileProperty property : propertyMap) {
            if (property.getName().equals("textures")) {
                return property.getValue();
            }
        }
        return null;
    }

    public static String getAvatarUrl(PlayerProfile profile) {
        return constructAvatarUrl(profile.getName(), Objects.requireNonNull(profile.getId()), getTexture(profile));
    }

    public static String getAvatarUrl(String name, UUID uuid, String texture) {
        return constructAvatarUrl(name, uuid, texture);
    }

    private static String constructAvatarUrl(String username, UUID uuid, String texture) {

        String defaultUrl = "https://crafatar.com/avatars/{uuid-nodashes}.png?size={size}&overlay#{texture}";
        //String offlineUrl = "https://cravatar.eu/helmavatar/{username}/{size}.png#{texture}";

        username = URLEncoder.encode(username, StandardCharsets.UTF_8);

        defaultUrl = defaultUrl
                .replace("{texture}", texture != null ? texture : "")
                .replace("{username}", username)
                .replace("{uuid}", uuid != null ? uuid.toString() : "")
                .replace("{uuid-nodashes}", Objects.requireNonNull(uuid).toString().replace("-", ""))
                .replace("{size}", "128");

        return defaultUrl;
    }
}
