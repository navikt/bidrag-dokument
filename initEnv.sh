kubectx dev-fss
kubectl exec --tty deployment/bidrag-dokument -- printenv | grep -E 'AZURE_|TOKEN_X|_URL|SCOPE|CLIENT_ID' | grep -v -e 'BIDRAG_FORSENDELSE_URL'> src/test/resources/application-lokal-nais-secrets.properties
