const assert = require('assert');

const {
    Given,
    When,
    Then
} = require('cucumber');

const {
    httpGet
} = require('../support/fasit')

Given('restservice {string} i {string}', function (alias, env) {
    this.alias = alias;
    this.env = env;
})

Given('saksnummer {string}', function (saksnummer) {
    this.saksnummer = saksnummer;
})

When('jeg gjør get {string}', function (suffix, done) {
    httpGet(this.alias, this.env, suffix)
        .then(response => {
            this.response = response;
            done()
        })
        .catch(err => {
            this.error = err;
            this.response = null;
            done()
        })
});

Then('skal resultatet være en liste med journalposter', function () {
    assert.ok(Array.isArray(this.response.data), "resultatet er ikke en liste: " + JSON.stringify(this.list));
});

Then('skal resultatet være et journalpost objekt', function () {
    var data = this.response ? this.response.data : null;
    assert.ok(data != null, "posten finnes ikke");
    assert.ok(data.jp_id != null, "journalposten mangler påkrevde properties");
});

Then('skal resultatet ikke være et journalpost objekt', function () {
    var data = this.response ? this.response.data : null;
    assert.ok(data == null || data.length == 0, "requested object is not null/empty: " + JSON.stringify(data));
});

Then('statuskoden skal være {string}', function (status) {
    assert.ok(this.response.status == status, "actual status: " + this.response.status)
});

Then('hver journalpost i listen skal ha saksnummer {string} i {string} feltet', function (saksnummer, prop) {
    var arr = this.response.data.filter(jp => jp[prop] == saksnummer);
    assert.ok(arr.length == this.response.data.length, "Det finnes forskjellige saksnummer i listen!")
});

Then('objektet skal inneholde følgende verdier', function (data) {
    var expected = JSON.parse(data)
    assert.deepEqual(this.response.data, expected, "Objektene er forskjellige");
})

Then('hver rad i listen skal ha følgende properties satt:', function (table) {
    var missing = [];
    this.response.data.forEach(row => {
        table.rawTable.forEach(item => {
            if (!row[item[0]]) {
                console.log("-- mangler", item[0], "i", row)
                missing.push(item[0])
            }
        })
    })
    if (missing.length > 0) {
        throw "manglende properties";
    }
})