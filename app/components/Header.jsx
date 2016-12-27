import React from 'react'
import ophLogo from '../resources/img/logo_oph.svg'
import okmLogo from '../resources/img/logo_okm.png'

const imgStyles = {
};

const Header = ({lang, changeLang}) => {

  const langLinks = [{id:"fi", text: "suomeksi"}, {id:"sv", text:"på svenska"}]
    .map((s, i) => <li key={i}>
      {s.id !== lang ?
        <a href="#" onClick={changeLang(s.id)}>{s.text}</a> :
        <span>{s.text}</span>
      }
    </li>);

  return(
  <div className="container-fluid">
    <div id="header" className="row">
        <div style={imgStyles} className="headerImages col-xs-12 col-md-9">
          <img src={ophLogo}/> <img src={okmLogo} />
        </div>
      <div>
      <ul className="languageSelector col-xs-12 col-md-3">
        {langLinks}
      </ul>
      </div>
    </div>
  </div>)
};

export default Header;