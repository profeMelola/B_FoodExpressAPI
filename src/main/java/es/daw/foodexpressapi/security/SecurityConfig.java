package es.daw.foodexpressapi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity // Habilita @PreAuthorize y @PostAuthorize
@RequiredArgsConstructor
//@EnableWebSecurity // No es necesario con Spring Boot 3.x / Spring Security 6.x
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                /*
                    Por defecto, los navegadores bloquean las peticiones entre dominios distintos.

                    CORS (Cross-Origin Resource Sharing) es un mecanismo del navegador que protege al usuario frente a peticiones no deseadas entre dominios distintos.

                    Si tu frontend y backend no están en el mismo origen, tienes que habilitar CORS. Por ejemplo:
                        - Tu frontend está en http://localhost:8080
                        - Tu backend (API REST) está en http://localhost:8081

                    Aunque sean ambos “localhost”, son distintos orígenes (porque el puerto cambia).
                    Por defecto, el navegador bloquea esas peticiones.

                    Si no defines los métodos (setAllowedMethods), headers (setAllowedHeaders) ni allowCredentials (setAllowCredentials), tu API:
                    - No aceptará peticiones del frontend.
                    - No permitirá que le envíen tokens en el header Authorization.

                    ¿Por qué OPTIONS?
                        Cuando haces una petición POST o DELETE con Authorization, el navegador envía una preflight request con método OPTIONS.
                        Si no permites OPTIONS, el navegador bloquea la petición real.
                        Si no permites OPTIONS, las peticiones con token JWT nunca llegarán al backend.
                 */
//                .cors(cors -> cors.configurationSource(request -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.setAllowedOrigins(List.of("http://localhost:8080")); // Cambia al origen de tu frontend
//                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
//                    config.setAllowCredentials(true); // Si usas cookies o tokens en headers
//                    return config;
//                }))

                /*
                CSRF (Cross-Site Request Forgery) es un tipo de ataque donde un sitio malicioso puede hacer que un navegador autenticado
                (por ejemplo, con una cookie activa) haga una petición no deseada a otro sitio en el que el usuario está logueado.
                Es un ataque clásico en aplicaciones web basadas en sesiones y cookies.
                Su objetivo es hacer que el navegador del usuario realice una acción sin su consentimiento aprovechando que ya tiene una sesión abierta.
                 */
                /*
                ¿Por qué NO necesitas CSRF en una API REST?
                    - No usas cookies para la autenticación, sino tokens JWT que van en el header (normalmente en Authorization: Bearer ...).
                    - No hay formularios web ni sesiones HTML (como en aplicaciones MVC tradicionales).
                    - En APIs REST las peticiones maliciosas no pueden inyectar headers personalizados como Authorization desde un navegador,
                      por lo que el riesgo de CSRF no aplica.
                 */
                .csrf(csrf -> csrf.disable())
                // Le dice a Spring Security cómo debe manejar las sesiones HTTP
                // En una API REST con JWT, no se usan sesiones.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // permitir iframes (para H2)
                // Esto actúa antes del controlador
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**","/h2-console/**").permitAll() // pública para login/register
                        .requestMatchers(HttpMethod.GET,"/api/**").permitAll() // si todos los get son público
                        .requestMatchers(HttpMethod.POST,"/api/restaurants").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Sin este bean, Spring no sabrá cómo inyectar AuthenticationManager en tus clases.
    // Lo usamos en AuthController
    // No lo necesitas si todo el proceso de autenticación lo maneja Spring automáticamente, como cuando usas formLogin()
    // En una API REST con JWT, donde tú haces la autenticación manualmente y devuelves un token (como tú estás haciendo), sí lo necesitas.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
