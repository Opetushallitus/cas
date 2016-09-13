import React from 'react'
import ophLogo from '../resources/img/logo_oph.png'
import okmLogo from '../resources/img/logo_okm.png'

const imgStyles = {
};

export default class Header extends React.Component {

  render(){
    const controller = this.props.controller
    const lang = this.props.lang
    const langLinks = [{id:"fi", text: "suomeksi"}, {id:"sv", text:"på svenska"}]
      .map((s, i) => <li key={i}>
        {s.id !== lang ?
          <a href="#" onClick={controller.changeLang(s.id)}>{s.text}</a> :
          <span>{s.text}</span>
        }
      </li>);

    return(
    <div className="container-fluid">
      <div id="header" className="row">
          <div style={imgStyles} className="col-xs-6 col-md-9">
            <img src={ophLogo}/> <img src={okmLogo} />
          </div>
        <div>
        <ul className="languageSelector col-xs-6 col-md-3">
          {langLinks}
        </ul>
        </div>
      </div>
    </div>
    )
  }
}