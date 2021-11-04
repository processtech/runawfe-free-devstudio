package ru.runa.gpd.connector.wfe.ws;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.BotAPI;
import ru.runa.wfe.webservice.BotWebService;
import ru.runa.wfe.webservice.DataSourceAPI;
import ru.runa.wfe.webservice.DataSourceWebService;
import ru.runa.wfe.webservice.DefinitionAPI;
import ru.runa.wfe.webservice.DefinitionWebService;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.Login;
import ru.runa.wfe.webservice.Relation;
import ru.runa.wfe.webservice.RelationAPI;
import ru.runa.wfe.webservice.RelationWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfExecutor;

public class WebServiceWfeServerConnector extends WfeServerConnector {
    private User user;

    @Override
    public void connect() {
        AuthenticationAPI api = getAuthenticationService();
        if (AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(settings.getAuthenticationType())) {
            String password = settings.getPassword();
            if (password == null) {
                return;
            }
            user = api.authenticateByLoginPassword(settings.getLogin(), password);
        } else {
            user = api.authenticateByKerberos(getKerberosToken());
        }
    }

    @Override
    public void disconnect() throws Exception {
        user = null;
    }

    @Override
    public Map<String, Boolean> getExecutors() {
        List<WfExecutor> executors = getExecutorService().getExecutors(getUser(), null);
        Map<String, Boolean> result = Maps.newHashMapWithExpectedSize(executors.size());
        for (WfExecutor executor : executors) {
            if (Actor.class.getName().equals(executor.getExecutorClassName())) {
                result.put(executor.getName(), false);
            }
            if (Group.class.getName().equals(executor.getExecutorClassName())) {
                result.put(executor.getName(), true);
            }
        }
        return result;
    }

    @Override
    public List<String> getRelationNames() {
        RelationAPI api = getRelationService();
        List<Relation> relations = api.getRelations(getUser(), null);
        List<String> result = Lists.newArrayListWithExpectedSize(relations.size());
        for (Relation relation : relations) {
            result.add(relation.getName());
        }
        return result;
    }

    @Override
    public List<WfDefinition> getProcessDefinitions() {
        DefinitionAPI api = getDefinitionService();
        return WfDefinitionAdapter.toDTOs(api.getProcessDefinitions(getUser(), null, false));
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(WfDefinition definition) {
        DefinitionAPI api = getDefinitionService();
        List<WfDefinition> list = WfDefinitionAdapter.toDTOs(api.getProcessDefinitionHistory(getUser(), definition.getName()));
        if (!list.isEmpty()) {
            list.remove(0);
        }
        return list;
    }

    @Override
    public byte[] getProcessDefinitionArchive(WfDefinition definition) {
        return getDefinitionService().getProcessDefinitionFile(getUser(), definition.getVersionId(), "par");
    }

    @Override
    public WfDefinition deployProcessDefinitionArchive(byte[] par) {
        try {
            return WfDefinitionAdapter.toDTO(getDefinitionService().deployProcessDefinition(getUser(), par, Lists.newArrayList("GPD"), null));
        } catch (Exception e) {
            if (e.getMessage() != null && (e.getMessage().contains("DefinitionAlreadyExistException") || e.getMessage().contains("already exists"))) {
                throw new DefinitionAlreadyExistException("");
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public WfDefinition updateProcessDefinitionArchive(Long definitionId, byte[] par) {
        try {
            return WfDefinitionAdapter.toDTO(getDefinitionService().updateProcessDefinition(getUser(), definitionId, par));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("DefinitionDoesNotExistException")) {
                throw new DefinitionDoesNotExistException(String.valueOf(definitionId));
            }
            if (e.getMessage() != null && e.getMessage().contains("Definition") && e.getMessage().contains("does not exist")) {
                // jboss4
                throw new DefinitionDoesNotExistException(String.valueOf(definitionId));
            }
            if (e.getMessage() != null && e.getMessage().contains("DefinitionNameMismatchException")) {
                throw new DefinitionNameMismatchException("", "");
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types) {
        try {
            return WfDefinitionAdapter.toDTO(getDefinitionService().redeployProcessDefinition(getUser(), definitionId, par, types, null));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("DefinitionDoesNotExistException")) {
                throw new DefinitionDoesNotExistException(String.valueOf(definitionId));
            }
            if (e.getMessage() != null && e.getMessage().contains("Definition") && e.getMessage().contains("does not exist")) {
                // jboss4
                throw new DefinitionDoesNotExistException(String.valueOf(definitionId));
            }
            if (e.getMessage() != null && e.getMessage().contains("DefinitionNameMismatchException")) {
                throw new DefinitionNameMismatchException("", "");
            }
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Map<Bot, List<BotTask>> getBots() {
        User user = getUser();
        Map<Bot, List<BotTask>> result = Maps.newHashMap();
        List<BotStation> botStations = BotStationAdapter.toDTOs(getBotService().getBotStations());
        for (BotStation botStation : botStations) {
            for (Bot bot : BotAdapter.toDTOs(getBotService().getBots(user, botStation.getId()))) {
                result.put(bot, BotTaskAdapter.toDTOs(getBotService().getBotTasks(user, bot.getId())));
            }
        }
        return result;
    }

    @Override
    public byte[] getBotFile(Bot bot) {
        return getBotService().exportBot(getUser(), BotAdapter.toJAXB(bot));
    }

    @Override
    public byte[] getBotTaskFile(Bot bot, String botTask) {
        return getBotService().exportBotTask(getUser(), BotAdapter.toJAXB(bot), botTask);
    }

    @Override
    public void deployBot(String botStationName, byte[] archive) {
        BotStation botStation = BotStationAdapter.toDTO(getBotService().getBotStationByName(botStationName));
        if (botStation == null) {
            throw new BotStationDoesNotExistException(botStationName);
        }
        getBotService().importBot(getUser(), BotStationAdapter.toJAXB(botStation), archive, true);
    }

    @Override
    public byte[] getBotStationFile(BotStation botStation) {
        return getBotService().exportBotStation(getUser(), BotStationAdapter.toJAXB(botStation));
    }

    @Override
    public void deployBotStation(byte[] archive) {
        getBotService().importBotStation(getUser(), archive, true);
    }

    @Override
    public List<BotStation> getBotStations() {
        return BotStationAdapter.toDTOs(getBotService().getBotStations());
    }

    @Override
    public void deployDataSourceArchive(byte[] archive) {
        getDataSourceService().importDataSource(getUser(), archive);
    }

    @Override
    public byte[] getDataSourceArchive(String dsName) {
        return getDataSourceService().exportDataSource(getUser(), dsName);
    }

    @Override
    public List<String> getDataSourceNames() {
        return getDataSourceService().getNames();
    }

    private String getServiceUrl(String serviceName) {
        String version = getVersion();
        return settings.getUrl() + "/wfe-service-" + version + "/" + serviceName + "WebService/" + serviceName + "API";
    }

    private void setApiEndpointAddress(Object api, String serviceUrl) {
        BindingProvider bindingProvider = (BindingProvider) api;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);
    }

    private URL getWsdlUrl(String serviceUrl) {
        try {
            return new URL(serviceUrl + "?wsdl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getVersion() {
        String version = settings.getVersion();
        if (version == null) {
            String url = settings.getUrl() + "/wfe/version";
            try {
                if ("https".equals(settings.getProtocol())) {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    if (settings.isAllowSslInsecure()) {
                        TrustManager[] tr = new TrustManager[] { new javax.net.ssl.X509TrustManager() {

                            @Override
                            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                            }

                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }

                        } };
                        sslContext.init(null, tr, new SecureRandom());

                        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                        });
                    } else {
                        sslContext.init(null, null, new SecureRandom());
                        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname, SSLSession session) {
                                return false;
                            }
                        });
                    }
                    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                }
                InputStreamReader reader = new InputStreamReader(new URL(url).openStream());
                version = CharStreams.toString(reader);
                int colonIndex = version.indexOf(":");
                if (colonIndex != -1) {
                    version = version.substring(colonIndex + 1);
                }
                reader.close();
            } catch (SSLHandshakeException sslEx) {
                throw new RuntimeException("Ssl certificate is invalid for " + url);
            } catch (Exception e) {
                throw new RuntimeException("Unable to acquire version using " + url, e);
            }
            settings.setVersion(version);
        }
        return version;
    }

    private User getUser() {
        if (user == null) {
            connect();
        } else {
            try {
                // check user is up to date (dynamic way allows changes API in neighbor methods)
                Service service = Service.create(new QName("http://impl.service.wfe.runa.ru/", "SystemWebService"));
                QName portQName = new QName("http://impl.service.wfe.runa.ru/", "SystemAPI");
                service.addPort(portQName, SOAPBinding.SOAP11HTTP_BINDING, getServiceUrl("System"));
                JAXBContext jaxbContext = JAXBContext.newInstance(Login.class, LoginResponse.class);
                Dispatch<Object> dispatch = service.createDispatch(portQName, jaxbContext, Service.Mode.PAYLOAD);
                Login login = new Login();
                login.setUser(user);
                // dispatch.invokeOneWay(..) does not work here (did not show error) but in simple program does...
                dispatch.invoke(new JAXBElement<Login>(new QName("http://impl.service.wfe.runa.ru/", "login"), Login.class, login));
            } catch (SOAPFaultException e) {
                if (e.getMessage() == null || !e.getMessage().contains("Error in subject decryption")) {
                    Throwables.propagate(e);
                }
                connect();
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
        return user;
    }

    private AuthenticationAPI getAuthenticationService() {
        String serviceUrl = getServiceUrl("Authentication");
        AuthenticationAPI api = new AuthenticationWebService(getWsdlUrl(serviceUrl)).getAuthenticationAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    private ExecutorAPI getExecutorService() {
        String serviceUrl = getServiceUrl("Executor");
        ExecutorAPI api = new ExecutorWebService(getWsdlUrl(serviceUrl)).getExecutorAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    private DefinitionAPI getDefinitionService() {
        String serviceUrl = getServiceUrl("Definition");
        DefinitionAPI api = new DefinitionWebService(getWsdlUrl(serviceUrl)).getDefinitionAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    private BotAPI getBotService() {
        String serviceUrl = getServiceUrl("Bot");
        BotAPI api = new BotWebService(getWsdlUrl(serviceUrl)).getBotAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    private DataSourceAPI getDataSourceService() {
        String serviceUrl = getServiceUrl("DataSource");
        DataSourceAPI api = new DataSourceWebService(getWsdlUrl(serviceUrl)).getDataSourceAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    private RelationAPI getRelationService() {
        String serviceUrl = getServiceUrl("Relation");
        RelationAPI api = new RelationWebService(getWsdlUrl(serviceUrl)).getRelationAPIPort();
        setApiEndpointAddress(api, serviceUrl);
        return api;
    }

    // ru.runa.wfe.webservice.LoginResponse cannot be used due to lack of @XmlRootElement
    @XmlRootElement(namespace = "http://impl.service.wfe.runa.ru/", name = "loginResponse")
    private static class LoginResponse {

    }

}
