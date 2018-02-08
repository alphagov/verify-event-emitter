package uk.gov.ida.eventemitter;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

public class AmazonEncrypter implements Encrypter {

    private final AwsCrypto awsCrypto;
    private final KmsMasterKeyProvider provider;
    private final ObjectMapper mapper;

    public AmazonEncrypter(final AwsCrypto awsCrypto,
                           final KmsMasterKeyProvider provider,
                           final ObjectMapper mapper) {
        this.awsCrypto = awsCrypto;
        this.mapper = mapper;
        this.provider = provider;
    }

    @Override
    public String encrypt(final Event event) throws JsonProcessingException {
        final Map<String, String> context = Collections.EMPTY_MAP;
        return awsCrypto.encryptString(provider, mapper.writeValueAsString(event), context).getResult();
    }
}
