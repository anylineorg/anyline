package org.anyline.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserAgentGenerator {

    public static final String[] WINDOWS_PLATFORMS = {
            "Windows NT 10.0; Win64; x64", "Windows NT 6.1; Win64; x64",
            "Windows NT 6.2; Win64; x64", "Windows NT 10.0; WOW64",
            "Windows NT 6.1; WOW64", "Windows NT 6.0; Win64; x64",
            "Windows NT 5.1; Win64; x64", "Windows NT 6.3; Win64; x64",
            "Windows NT 10.0; Win64; x64; ARM64", "Windows NT 10.0; Win64; x64; ARM",
            "Windows NT 6.1; Win64; x64; WOW64", "Windows NT 6.2; Win64; x64; WOW64",
            "Windows NT 10.0; Win64; x64; x64", "Windows NT 6.1; Win64; x64; x64",
            "Windows NT 6.0; Win64; x64; x64", "Windows NT 5.1; Win64; x64; x64",
            "Windows NT 6.3; Win64; x64; x64", "Windows NT 10.0; Win64; x64; x64; ARM64",
            "Windows NT 10.0; Win64; x64; x64; ARM", "Windows NT 10.0; Win64; x64; x64; x64"
    };

    public static final String[] MAC_PLATFORMS = {
            "Macintosh; Intel Mac OS X 10_15_7", "Macintosh; Intel Mac OS X 11_6_2",
            "Macintosh; Intel Mac OS X 12_1", "Macintosh; Intel Mac OS X 13_0",
            "Macintosh; Intel Mac OS X 10_14_6", "Macintosh; Intel Mac OS X 10_13_6",
            "Macintosh; Intel Mac OS X 10_12_6", "Macintosh; Intel Mac OS X 10_11_6",
            "Macintosh; Intel Mac OS X 10_10_5", "Macintosh; Intel Mac OS X 10_9_5",
            "Macintosh; Intel Mac OS X 10_8_5", "Macintosh; Intel Mac OS X 10_7_5",
            "Macintosh; Intel Mac OS X 10_6_8", "Macintosh; Intel Mac OS X 10_5_8",
            "Macintosh; Intel Mac OS X 10_4_11", "Macintosh; Intel Mac OS X 10_3_9",
            "Macintosh; Intel Mac OS X 10_2_8", "Macintosh; Intel Mac OS X 10_1_5",
            "Macintosh; Intel Mac OS X 10_0_4", "Macintosh; Intel Mac OS X 9_2_2"
    };

    public static final String[] LINUX_PLATFORMS = {
            "X11; Linux x86_64", "X11; Ubuntu; Linux x86_64",
            "X11; Linux i686", "X11; Debian; Linux x86_64",
            "X11; Fedora; Linux x86_64", "X11; CentOS; Linux x86_64",
            "X11; Arch; Linux x86_64", "X11; Mint; Linux x86_64",
            "X11; Kali; Linux x86_64", "X11; Red Hat; Linux x86_64",
            "X11; openSUSE; Linux x86_64", "X11; Slackware; Linux x86_64",
            "X11; Gentoo; Linux x86_64", "X11; Manjaro; Linux x86_64",
            "X11; Elementary; Linux x86_64", "X11; Pop!_OS; Linux x86_64",
            "X11; Zorin; Linux x86_64", "X11; LinuxMint; Linux x86_64",
            "X11; Ubuntu; Linux i686", "X11; Debian; Linux i686"
    };

    public static final Random random = new Random();

    // 动态生成Chrome版本数组
    public static String[] generateChromeVersions() {
        List<String> versions = new ArrayList<>();
        String[] baseVersions = {"98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117"};
        String[] subVersions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] buildVersions = {"4758", "4844", "4896", "4951", "5005", "5060", "5112", "5244", "5249", "5304", "5359", "5414", "5481", "5563", "5615", "5672", "5735", "5790", "5845", "5938"};

        for (int i = 0; i < 120; i++) {
            String major = baseVersions[random.nextInt(baseVersions.length)];
            String minor = subVersions[random.nextInt(subVersions.length)];
            String build = buildVersions[random.nextInt(buildVersions.length)];
            String patch = subVersions[random.nextInt(subVersions.length)];
            versions.add("Chrome/" + major + "." + minor + "." + build + "." + patch);
        }

        return versions.toArray(new String[0]);
    }

    // 动态生成Firefox版本数组
    public static String[] generateFirefoxVersions() {
        List<String> versions = new ArrayList<>();
        String[] baseVersions = {"97", "98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116"};

        for (int i = 0; i < 120; i++) {
            String version = baseVersions[random.nextInt(baseVersions.length)];
            String subVersion = java.lang.String.valueOf(random.nextInt(10));
            versions.add("Firefox/" + version + "." + subVersion);
        }

        return versions.toArray(new String[0]);
    }

    // 动态生成Safari版本数组
    public static String[] generateSafariVersions() {
        List<String> versions = new ArrayList<>();
        String[] baseVersions = {"605", "14", "15", "16", "17"};
        String[] subVersions = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] patchVersions = {"15", "16", "17", "18", "19", "20"};

        for (int i = 0; i < 120; i++) {
            String major = baseVersions[random.nextInt(baseVersions.length)];
            String minor = subVersions[random.nextInt(subVersions.length)];
            String patch = patchVersions[random.nextInt(patchVersions.length)];
            versions.add("Safari/" + major + "." + minor + "." + patch);
        }

        return versions.toArray(new String[0]);
    }

    // 动态生成Edge版本数组
    public static String[] generateEdgeVersions() {
        List<String> versions = new ArrayList<>();
        String[] baseVersions = {"98", "99", "100", "101", "102", "103", "104", "105", "106", "107", "108", "109", "110", "111", "112", "113", "114", "115", "116", "117"};
        String[] subVersions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        String[] buildVersions = {"1108", "1150", "1185", "1210", "1245", "1264", "1293", "1343", "1370", "1418", "1462", "1518", "1587", "1661", "1722", "1774", "1823", "1901", "1938", "2045"};

        for (int i = 0; i < 120; i++) {
            String major = baseVersions[random.nextInt(baseVersions.length)];
            String minor = subVersions[random.nextInt(subVersions.length)];
            String build = buildVersions[random.nextInt(buildVersions.length)];
            String patch = subVersions[random.nextInt(subVersions.length)];
            versions.add("Edg/" + major + "." + minor + "." + build + "." + patch);
        }

        return versions.toArray(new String[0]);
    }

    // 初始化动态生成的版本数组
    public static final String[] CHROME_VERSIONS = generateChromeVersions();
    public static final String[] FIREFOX_VERSIONS = generateFirefoxVersions();
    public static final String[] SAFARI_VERSIONS = generateSafariVersions();
    public static final String[] EDGE_VERSIONS = generateEdgeVersions();

    public static String random() {
        String[] browsers = {"chrome", "firefox", "safari", "edge"};
        String browser = browsers[random.nextInt(browsers.length)];

        switch (browser) {
            case "chrome":
                return generateChromeUserAgent();
            case "firefox":
                return generateFirefoxUserAgent();
            case "safari":
                return generateSafariUserAgent();
            case "edge":
                return generateEdgeUserAgent();
            default:
                return generateChromeUserAgent();
        }
    }

    public static String generateChromeUserAgent() {
        String[] platforms = {getRandomWindowsPlatform(), getRandomMacPlatform(), getRandomLinuxPlatform()};
        String platform = platforms[random.nextInt(platforms.length)];

        StringBuilder ua = new StringBuilder("Mozilla/5.0 (");
        ua.append(platform).append(") AppleWebKit/537.36 (KHTML, like Gecko) ");
        ua.append(getRandomChromeVersion()).append(" Safari/537.36");

        return ua.toString();
    }

    public static String generateFirefoxUserAgent() {
        String[] platforms = {getRandomWindowsPlatform(), getRandomMacPlatform(), getRandomLinuxPlatform()};
        String platform = platforms[random.nextInt(platforms.length)];

        StringBuilder ua = new StringBuilder("Mozilla/5.0 (");
        ua.append(platform).append("; rv:").append(getRandomFirefoxVersion().replace("Firefox/", "")).append(") Gecko/20100101 ");
        ua.append(getRandomFirefoxVersion());

        return ua.toString();
    }

    public static String generateSafariUserAgent() {
        String platform = getRandomMacPlatform();
        String version = getRandomSafariVersion();

        StringBuilder ua = new StringBuilder("Mozilla/5.0 (");
        ua.append(platform).append(") AppleWebKit/605.1.15 (KHTML, like Gecko) Version/");
        ua.append(version.replace("Safari/", "")).append(" ").append(version);

        return ua.toString();
    }

    public static String generateEdgeUserAgent() {
        String platform = getRandomWindowsPlatform();
        String version = getRandomEdgeVersion();

        StringBuilder ua = new StringBuilder("Mozilla/5.0 (");
        ua.append(platform).append(") AppleWebKit/537.36 (KHTML, like Gecko) ");
        ua.append(version).append(" Safari/537.36");

        return ua.toString();
    }

    public static String getRandomChromeVersion() {
        return CHROME_VERSIONS[random.nextInt(CHROME_VERSIONS.length)];
    }

    public static String getRandomFirefoxVersion() {
        return FIREFOX_VERSIONS[random.nextInt(FIREFOX_VERSIONS.length)];
    }

    public static String getRandomSafariVersion() {
        return SAFARI_VERSIONS[random.nextInt(SAFARI_VERSIONS.length)];
    }

    public static String getRandomEdgeVersion() {
        return EDGE_VERSIONS[random.nextInt(EDGE_VERSIONS.length)];
    }

    public static String getRandomWindowsPlatform() {
        return WINDOWS_PLATFORMS[random.nextInt(WINDOWS_PLATFORMS.length)];
    }

    public static String getRandomMacPlatform() {
        return MAC_PLATFORMS[random.nextInt(MAC_PLATFORMS.length)];
    }

    public static String getRandomLinuxPlatform() {
        return LINUX_PLATFORMS[random.nextInt(LINUX_PLATFORMS.length)];
    }
}
