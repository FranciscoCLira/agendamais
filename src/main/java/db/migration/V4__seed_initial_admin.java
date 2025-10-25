package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Flyway Java migration to seed a minimal initial admin user and institution.
 *
 * Creates rows in: instituicao, pessoa, usuario, usuario_instituicao
 */
public class V4__seed_initial_admin extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        var connection = context.getConnection();

        long instituicaoId;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO instituicao (nome_instituicao, situacao_instituicao, data_ultima_atualizacao, email_instituicao) VALUES (?, 'A', CURRENT_DATE, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Instituto Inicial");
            ps.setString(2, "instituto-inicial@example.com");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    instituicaoId = rs.getLong(1);
                } else {
                    throw new IllegalStateException("Failed to obtain generated id for instituicao");
                }
            }
        }

        long pessoaId;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO pessoa (nome_pessoa, situacao_pessoa, email_pessoa, celular_pessoa, data_inclusao, data_ultima_atualizacao, curriculo_pessoal) VALUES (?, 'A', ?, ?, CURRENT_DATE, CURRENT_DATE, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Administrador Inicial");
            ps.setString(2, "admin@example.com");
            ps.setString(3, "+550000000000");
            ps.setString(4, "Conta administrativa inicial");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    pessoaId = rs.getLong(1);
                } else {
                    throw new IllegalStateException("Failed to obtain generated id for pessoa");
                }
            }
        }

    // Read admin credentials from environment variables, with safe defaults for testing.
    String adminUsername = System.getenv().getOrDefault("ADMIN_USERNAME", "admin1");
    String adminPassword = System.getenv().getOrDefault("ADMIN_PASSWORD", "admin1$");
    String adminEmail = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@example.com");
    String institutionName = System.getenv().getOrDefault("INSTITUTION_NAME", "Instituto Inicial");

    // Use BCrypt to hash the password before storing. Avoid printing the raw password.
    String hashed = new BCryptPasswordEncoder().encode(adminPassword);

        long usuarioId;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO usuario (username, password, situacao_usuario, pessoa_id, data_ultima_atualizacao) VALUES (?, ?, 'A', ?, CURRENT_DATE)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, adminUsername);
            ps.setString(2, hashed);
            ps.setLong(3, pessoaId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuarioId = rs.getLong(1);
                } else {
                    throw new IllegalStateException("Failed to obtain generated id for usuario");
                }
            }
        }

        // Link usuario to instituicao as SuperUsuario (nivel 9)
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO usuario_instituicao (usuario_id, instituicao_id, sit_acesso_usuario_instituicao, nivel_acesso_usuario_instituicao) VALUES (?, ?, 'A', 9)")) {
            ps.setLong(1, usuarioId);
            ps.setLong(2, instituicaoId);
            ps.executeUpdate();
        }

    System.out.println("[V4__seed_initial_admin] Created initial institution=" + instituicaoId + " pessoa=" + pessoaId
        + " usuario=" + usuarioId + " (username=" + adminUsername + ")");
    }
}
