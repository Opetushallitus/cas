import Flatten from 'flat'
import _ from 'lodash'
import * as translations from './resources/translations.json'
import services from './resources/services.json'
import {getCookie, setCookie} from './util.js'

_.mixin({
  merge : function() {
    return _.reduce(arguments, function(list, obj){
      return _.extend(list, obj);
    }, {});
  }
});

const flatTrans = Flatten(_.merge(translations, services));

export function translation(key, lang=resolveLang()) {
  const fullKey = key + "." + lang;
  const trans = flatTrans[fullKey];
  if (_.isEmpty(trans)) {
    return fullKey
  } else {
    return trans
  }
}

export function resolveLang() {
  return getCookie("i18next") || browserLanguage()
}

function browserLanguage(){
  var lang =  (navigator.language || navigator.userLanguage).substr(0,2);
  if(["fi", "sv"].indexOf(lang) >= 0) return lang; else return "fi"
}

export function changeLang(val) {
  setCookie("i18next", val);
  document.title = translation("app.documentTitle");
}
