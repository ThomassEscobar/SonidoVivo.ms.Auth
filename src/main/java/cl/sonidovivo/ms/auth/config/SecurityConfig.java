package cl.sonidovivo.ms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import cl.sonidovivo.ms.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    // 1. Configuramos las reglas de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1. Deshabilitar CSRF (la causa del 403 en peticiones POST)
        .csrf(AbstractHttpConfigurer::disable)
        // 2. Configurar autorización de rutas
        .authorizeHttpRequests(auth -> auth
            // 3. Permitir rutas de autenticación y Swagger
            .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            // 4. Permitir el despacho de errores (para evitar el "ghost 403" de Security 7)
            .requestMatchers("/error").permitAll()
            // 5. Cualquier otra petición requiere autenticación
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

    return http.build();
    }

    // 2. Le decimos a Spring cómo buscar usuarios en la base de datos
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // 3. Configuramos el encriptador de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 4. Configuramos el proveedor de autenticación
    // 4. Configuramos el proveedor de autenticación
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Se pasa el UserDetailsService directamente en el constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        // El PasswordEncoder se establece con el setter
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    // 5. Exponemos el AuthenticationManager (necesario para el AuthService)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
