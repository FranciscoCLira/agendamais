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
        // Safety guard: require explicit RUN_BOOTSTRAP=true to execute this migration.
        String runBootstrap = System.getenv("RUN_BOOTSTRAP");
        if (runBootstrap == null || !"true".equalsIgnoreCase(runBootstrap)) {
            System.out.println(
                    "[V4__seed_initial_admin] RUN_BOOTSTRAP!=true — skipping bootstrap migration (set RUN_BOOTSTRAP=true to enable).");
            return;
        }

        String institutionName = System.getenv().getOrDefault("INSTITUTION_NAME", "Instituto Inicial");
        String institutionEmail = System.getenv().getOrDefault("INSTITUTION_EMAIL", "instituto-inicial@example.com");

        // Create or find institution
        long instituicaoId = -1;
        try (PreparedStatement psFind = connection
                .prepareStatement("SELECT id FROM instituicao WHERE nome_instituicao = ?")) {
            psFind.setString(1, institutionName);
            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) {
                    instituicaoId = rs.getLong(1);
                }
            }
        }
        if (instituicaoId == -1) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO instituicao (nome_instituicao, situacao_instituicao, data_ultima_atualizacao, email_instituicao) VALUES (?, 'A', CURRENT_DATE, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, institutionName);
                ps.setString(2, institutionEmail);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        instituicaoId = rs.getLong(1);
                    } else {
                        throw new IllegalStateException("Failed to obtain generated id for instituicao");
                    }
                }
            }
        }

        // Helper lambda: create pessoa if absent by email
        java.util.function.Function<String, Long> createPessoaIfAbsent = (email) -> {
            try {
                long pessoaId = -1;
                try (PreparedStatement psFind = connection
                        .prepareStatement("SELECT id FROM pessoa WHERE email_pessoa = ?")) {
                    psFind.setString(1, email);
                    try (ResultSet rs = psFind.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong(1);
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO pessoa (nome_pessoa, situacao_pessoa, email_pessoa, celular_pessoa, data_inclusao, data_ultima_atualizacao, curriculo_pessoal) VALUES (?, 'A', ?, ?, CURRENT_DATE, CURRENT_DATE, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "Bootstrap - " + email);
                    ps.setString(2, email);
                    ps.setString(3, "+550000000000");
                    ps.setString(4, "Conta criada pelo bootstrap");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            pessoaId = rs.getLong(1);
                        } else {
                            throw new IllegalStateException("Failed to obtain generated id for pessoa");
                        }
                    }
                }
                return pessoaId;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // Helper lambda: create usuario if absent by username
        java.util.function.Function<java.util.Map<String, Object>, Long> createUsuarioIfAbsent = (map) -> {
            try {
                String username = (String) map.get("username");
                String password = (String) map.get("password");
                Long pessoaId = (Long) map.get("pessoaId");

                try (PreparedStatement psFind = connection
                        .prepareStatement("SELECT id FROM usuario WHERE username = ?")) {
                    psFind.setString(1, username);
                    try (ResultSet rs = psFind.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong(1);
                        }
                    }
                }

                String hashed = new BCryptPasswordEncoder().encode(password);
                long usuarioId = -1;
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO usuario (username, password, situacao_usuario, pessoa_id, data_ultima_atualizacao) VALUES (?, ?, 'A', ?, CURRENT_DATE)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
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
                return usuarioId;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // Process SUPERUSER
        String superUsername = System.getenv("SUPER_USERNAME");
        String superPassword = System.getenv("SUPER_PASSWORD");
        String superEmail = System.getenv("SUPER_EMAIL");
        Long superUsuarioId = null;
        if (superUsername != null && superPassword != null && superEmail != null) {
            long pessoaId = createPessoaIfAbsent.apply(superEmail);
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("username", superUsername);
            params.put("password", superPassword);
            params.put("pessoaId", pessoaId);
            superUsuarioId = createUsuarioIfAbsent.apply(params);
            // link as nivel 9
            try (PreparedStatement psCheck = connection.prepareStatement(
                    "SELECT id FROM usuario_instituicao WHERE usuario_id = ? AND instituicao_id = ?")) {
                psCheck.setLong(1, superUsuarioId);
                psCheck.setLong(2, instituicaoId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO usuario_instituicao (usuario_id, instituicao_id, sit_acesso_usuario_instituicao, nivel_acesso_usuario_instituicao) VALUES (?, ?, 'A', 9)")) {
                            ps.setLong(1, superUsuarioId);
                            ps.setLong(2, instituicaoId);
                            ps.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("[V4__seed_initial_admin] ensured superuser=" + superUsername + " id=" + superUsuarioId);
        } else {
            System.out.println(
                    "[V4__seed_initial_admin] SUPER_USERNAME/SUPER_PASSWORD/SUPER_EMAIL not fully defined; skipping superuser creation.");
        }

        // Process INSTITUTION ADMIN (nivel 5)
        String adminUsername = System.getenv("ADMIN_USERNAME");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        String adminEmail = System.getenv("ADMIN_EMAIL");
        Long adminUsuarioId = null;
        if (adminUsername != null && adminPassword != null && adminEmail != null) {
            long pessoaId = createPessoaIfAbsent.apply(adminEmail);
            java.util.Map<String, Object> params = new java.util.HashMap<>();
            params.put("username", adminUsername);
            params.put("password", adminPassword);
            params.put("pessoaId", pessoaId);
            adminUsuarioId = createUsuarioIfAbsent.apply(params);
            // link as nivel 5
            try (PreparedStatement psCheck = connection.prepareStatement(
                    "SELECT id FROM usuario_instituicao WHERE usuario_id = ? AND instituicao_id = ?")) {
                psCheck.setLong(1, adminUsuarioId);
                psCheck.setLong(2, instituicaoId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        try (PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO usuario_instituicao (usuario_id, instituicao_id, sit_acesso_usuario_instituicao, nivel_acesso_usuario_instituicao) VALUES (?, ?, 'A', 5)")) {
                            ps.setLong(1, adminUsuarioId);
                            ps.setLong(2, instituicaoId);
                            ps.executeUpdate();
                        }
                    }
                }
            }
            System.out.println(
                    "[V4__seed_initial_admin] ensured institution admin=" + adminUsername + " id=" + adminUsuarioId);
        } else {
            System.out.println(
                    "[V4__seed_initial_admin] ADMIN_USERNAME/ADMIN_PASSWORD/ADMIN_EMAIL not fully defined; skipping institution admin creation.");
        }

        System.out.println("[V4__seed_initial_admin] bootstrap complete for instituicaoId=" + instituicaoId);
    }
}
