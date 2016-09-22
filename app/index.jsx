import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
import App from './components/App';
import {initAppState, getController} from './appState.js'

require('./main.scss')
const appState = initAppState();

appState.onValue((state) => {
  ReactDOM.render(<App state={state} controller={getController()}/>, document.getElementById('app'));
});


// ReactDOM.render(<App />, document.getElementById('app'));

