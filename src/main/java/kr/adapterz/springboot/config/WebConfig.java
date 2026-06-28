package kr.adapterz.springboot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.post-image-dir:uploads/post-images}")
    private String postImageDir;

    @Value("${app.upload.profile-image-dir:uploads/profile-images}")
    private String profileImageDir;

    @Value("${app.upload.post-image-url-prefix:/uploads/post-images}")
    private String postImageUrlPrefix;

    @Value("${app.upload.profile-image-url-prefix:/uploads/profile-images}")
    private String profileImageUrlPrefix;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins(
				"http://localhost:3000",
				"http://127.0.0.1:3000",
				"http://localhost:5500",
				"http://127.0.0.1:5500"
			)
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true)
			.maxAge(3600);
	}

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(postImageUrlPrefix + "/**")
                .addResourceLocations(toResourceLocation(postImageDir));

        registry.addResourceHandler(profileImageUrlPrefix + "/**")
                .addResourceLocations(toResourceLocation(profileImageDir));
    }

    private String toResourceLocation(String imageDir) {
        return Path.of(imageDir).toAbsolutePath().normalize().toUri().toString();
    }
}
