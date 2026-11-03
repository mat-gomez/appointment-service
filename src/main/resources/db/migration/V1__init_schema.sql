CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          login VARCHAR(50) NOT NULL UNIQUE,
                          password_hash VARCHAR(60) NOT NULL,
                          first_name VARCHAR(50),
                          last_name VARCHAR(50),
                          email VARCHAR(254) UNIQUE,
                          image_url VARCHAR(256),
                          lang_key VARCHAR(10) DEFAULT 'es',
                          activated BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointment_reservation (
                                         id BIGSERIAL PRIMARY KEY,
                                         reservation_process_id VARCHAR(100) NOT NULL UNIQUE,
                                         hold_id VARCHAR(100),
                                         external_patient_id VARCHAR(100) NOT NULL,
                                         professional_id BIGINT NOT NULL,
                                         professional_first_name VARCHAR(50),
                                         professional_last_name VARCHAR(50),
                                         appointment_date DATE NOT NULL,
                                         start_time TIME NOT NULL,
                                         end_time TIME NOT NULL,
                                         status VARCHAR(50) NOT NULL,
                                         expires_at TIMESTAMP WITH TIME ZONE,
                                         request_event_id VARCHAR(100),
                                         reservation_id BIGINT,
                                         patient_phone VARCHAR(20),
                                         cancellation_reason VARCHAR(500),
                                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         confirmed_at TIMESTAMP WITH TIME ZONE,
                                         cancelled_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE processed_kafka_event (
                                       event_id VARCHAR(100) PRIMARY KEY,
                                       processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);