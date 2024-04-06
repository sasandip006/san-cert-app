package com.example.demo.cert.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.tcp.SslProvider;

@Slf4j
@Component
public class SslConfig {

    @Value("${sandh.client.ssl.trust-store}")
    private String trustStorePath;

    @Value("${sandh.client.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${sandh.client.ssl.key-store}")
    private String keyStorePath;

    @Value("${sandh.client.ssl.key-store-password}")
    private String keyStorePassword;

    /**
     * This method builds the SslContext which would hold Client KeyStore and
     * Client's TrustStore(Server's Cert) to be used during 2-way SSL handshake
     * 
     * @return SslContext - to be used by the {@link this#webClient()}
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws IOException
     * @throws CertificateException
     */
    public SslContext buildSslContextForReactorClientHttpConnector() throws UnrecoverableKeyException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        SslContext sslContext = null;
//        try (
        FileInputStream keyStoreFileInputStream = new FileInputStream(ResourceUtils.getFile(keyStorePath));
                FileInputStream trustStoreFileInputStream = new FileInputStream(
                ResourceUtils.getFile(trustStorePath));
//                ) {
            KeyStore keyStore = KeyStore.getInstance("jks");
            keyStore.load(keyStoreFileInputStream, keyStorePassword.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(trustStoreFileInputStream, trustStorePassword.toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(trustStore);

            sslContext = SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(trustManagerFactory)
                    .build();
//        } catch (Exception exception) {
//            log.error("Exception while building SSL context for reactor web client: ", exception);
//        }

        return sslContext;
    }

    /**
     * The bean is supposed to be used while making an REST call to the server.
     * 
     * This bean would have the
     * {@link this#buildSslContextForReactorClientHttpConnector()} keys and
     * certificate that would be exchanged during the handshake
     * 
     * @return webClient - to be wired where the server call expecting a 2-way SSL
     *         handshake
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     */
    @Bean
    public WebClient webClient() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        SslProvider sslProvider = SslProvider.builder().sslContext(buildSslContextForReactorClientHttpConnector())
                .build();
        reactor.netty.http.client.HttpClient httpClient = reactor.netty.http.client.HttpClient.create()
                .secure(sslProvider);
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

}