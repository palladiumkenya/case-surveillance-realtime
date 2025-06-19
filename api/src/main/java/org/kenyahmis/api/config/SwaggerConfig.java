package org.kenyahmis.api.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String OAUTH_SCHEME = "auth";
    @Value("${spring.security.oauth2.authorizationserver.endpoint.token-uri}")
    private String authURL;
    @Value("${springdoc.swagger-ui.server.url}")
    private String serverURL;
    @Bean
    public OpenAPI myOpenAPI() {
        Info info = new Info()
                .title("Case Surveillance API")
                .version("1.0");
        return new OpenAPI()
                .addServersItem(new Server().url(serverURL))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(OAUTH_SCHEME)
                )
                .components(new Components()
                        .addSecuritySchemes(OAUTH_SCHEME, createOAuthScheme()))
                .info(info);
    }

//    @Bean
//    public OpenApiCustomizer schemaCustomizer() {
//        Schema<NewCaseDto> newCaseSchema = new Schema<>();
//        return openApi -> {
//            openApi.getComponents().addSchemas("NewCase", new Schema<NewCaseDto>());
//        };
//    }

    private SecurityScheme createOAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(createOAuthFlows());
    }

    private OAuthFlows createOAuthFlows() {
        OAuthFlow clientCredentialsFlow = new OAuthFlow()
                .tokenUrl(authURL);
        return new OAuthFlows().clientCredentials(clientCredentialsFlow);
    }
}
