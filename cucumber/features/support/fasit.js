const axios = require('axios');
const fasitUrl = process.env.fasit || "https://fasit.adeo.no/api/v2/resources";

process.env.NODE_TLS_REJECT_UNAUTHORIZED='0'

function _hentUrl(data) {
    if(data && data.length > 0) {
        return data[0].properties.url
    }
    return null;
}

function hentFasitRessurs(ftype, alias, env) {
    return axios.get(fasitUrl, {
        params: {
            type: ftype,
            alias: alias,
            environment: env,
            usage: false
        }
    });
}

function hentFasitRestUrl(alias, env) {
    return hentFasitRessurs('RestService', alias, env)
        .then(response => {
            return _hentUrl(response.data);
        })
	.catch(err => {
	    console.log("ERROR", err)
	})
}


function httpGet(alias, env, suffix) {
    return hentFasitRestUrl(alias, env)
        .then(url => {
            return axios.get(url + suffix)
        })
	.catch(err => {
		console.log("ERROR", err)
		return null;
	})
}

module.exports = {
    httpGet,
    hentFasitRessurs,
    hentFasitRestUrl
};
