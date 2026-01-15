package dev.profitsoft.internship.rebrov.block_five.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusHistory {

    @Field(type = FieldType.Keyword)
    private MessageStatus status;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private Instant timestamp;

    @Field(type = FieldType.Text)
    private String details;
}