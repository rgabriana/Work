require 'spec_helper'
describe 'puppet' do

  context 'with defaults for all parameters' do
    it { should contain_class('puppet') }
  end
end
