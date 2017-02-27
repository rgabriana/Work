require 'spec_helper'
describe 'apache2' do

  context 'with defaults for all parameters' do
    it { should contain_class('apache2') }
  end
end
