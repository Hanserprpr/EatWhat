package you.v50to.eatwhat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.*;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule epochMillisModule = new SimpleModule();

        epochMillisModule.addSerializer(LocalDateTime.class, new StdSerializer<>(LocalDateTime.class) {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider){
                gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        });
        epochMillisModule.addSerializer(LocalDate.class, new StdSerializer<>(LocalDate.class) {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializationContext provider) {
                gen.writeNumber(value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
            }
        });
        epochMillisModule.addSerializer(OffsetDateTime.class, new StdSerializer<>(OffsetDateTime.class) {
            @Override
            public void serialize(OffsetDateTime value, JsonGenerator gen, SerializationContext provider) {
                gen.writeNumber(value.toInstant().toEpochMilli());
            }
        });
        epochMillisModule.addSerializer(Instant.class, new StdSerializer<>(Instant.class) {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializationContext provider) {
                gen.writeNumber(value.toEpochMilli());
            }
        });
        epochMillisModule.addSerializer(ZonedDateTime.class, new StdSerializer<>(ZonedDateTime.class) {
            @Override
            public void serialize(ZonedDateTime value, JsonGenerator gen, SerializationContext provider) {
                gen.writeNumber(value.toInstant().toEpochMilli());
            }
        });

        return JsonMapper.builder()
                .addModule(epochMillisModule)
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
